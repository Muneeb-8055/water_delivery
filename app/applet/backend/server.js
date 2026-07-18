const express = require('express');
const { Pool } = require('pg');

const app = express();
app.use(express.json());

const pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_NAME,
    password: process.env.DB_PASSWORD,
    port: process.env.DB_PORT || 5432,
});

// Haversine formula
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // Earth radius in meters
    const rad = Math.PI / 180;
    const dLat = (lat2 - lat1) * rad;
    const dLon = (lon2 - lon1) * rad;
    
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * rad) * Math.cos(lat2 * rad) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in meters
}

app.post('/api/v1/mobile/reconcile', async (req, res) => {
    const { tenant_id, driver_id, transactions, gps_logs } = req.body;
    
    if (!transactions || !Array.isArray(transactions)) {
        return res.status(400).json({ success: false, error: 'Invalid payload' });
    }

    const client = await pool.connect();
    
    try {
        await client.query('BEGIN');
        
        const reconciled = [];
        
        for (const tx of transactions) {
            // Check for idempotency using local_uuid
            const existingTx = await client.query(
                'SELECT local_uuid FROM transaction_ledger WHERE local_uuid = $1 AND tenant_id = $2',
                [tx.local_uuid || tx.localUuid, tenant_id] // handle both camelCase and snake_case depending on how it's sent
            );
            
            if (existingTx.rows.length > 0) {
                // Ignore gracefully
                reconciled.push(tx.local_uuid || tx.localUuid);
                continue;
            }
            
            // Get customer for geo validation
            const customerRes = await client.query(
                'SELECT geo_latitude, geo_longitude FROM customers WHERE id = $1 AND tenant_id = $2',
                [tx.customer_id || tx.customerId, tenant_id]
            );
            
            let visit_validation_state = 'UNVERIFIED';
            
            if (customerRes.rows.length > 0) {
                const customer = customerRes.rows[0];
                
                let driverLat = null;
                let driverLon = null;
                
                // Assuming we use the most recent GPS log or the tx has them
                if (gps_logs && gps_logs.length > 0) {
                    const epoch = tx.epoch_timestamp || tx.epochTimestamp;
                    const closestLog = gps_logs.reduce((prev, curr) => {
                        const prevEpoch = prev.epoch_timestamp || prev.epochTimestamp;
                        const currEpoch = curr.epoch_timestamp || curr.epochTimestamp;
                        return (Math.abs(currEpoch - epoch) < Math.abs(prevEpoch - epoch)) ? curr : prev;
                    });
                    driverLat = closestLog.latitude;
                    driverLon = closestLog.longitude;
                }
                
                if (driverLat != null && driverLon != null && customer.geo_latitude != null && customer.geo_longitude != null) {
                    const dist = calculateDistance(driverLat, driverLon, customer.geo_latitude, customer.geo_longitude);
                    if (dist <= 50) {
                        visit_validation_state = 'VERIFIED';
                    }
                }
            }
            
            // Insert transaction
            await client.query(
                `INSERT INTO transaction_ledger 
                (tenant_id, local_uuid, customer_id, record_classification, amount_charged, amount_collected, visit_validation_state, epoch_timestamp) 
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
                [
                    tenant_id,
                    tx.local_uuid || tx.localUuid,
                    tx.customer_id || tx.customerId,
                    tx.record_classification || tx.recordClassification,
                    tx.amount_charged || tx.amountCharged,
                    tx.amount_collected || tx.amountCollected,
                    visit_validation_state,
                    tx.epoch_timestamp || tx.epochTimestamp
                ]
            );
            
            // Update customer balance & empties
            await client.query(
                `UPDATE customers 
                 SET receivable_balance = receivable_balance + $1 - $2,
                     company_owned_bottles = company_owned_bottles + $3 - $4
                 WHERE id = $5 AND tenant_id = $6`,
                [
                    tx.amount_charged || tx.amountCharged || 0,
                    tx.amount_collected || tx.amountCollected || 0,
                    tx.item_units_delivered || tx.itemUnitsDelivered || 0, // Bottles left with customer
                    tx.package_assets_recovered || tx.packageAssetsRecovered || 0, // Empties recovered
                    tx.customer_id || tx.customerId,
                    tenant_id
                ]
            );
            
            reconciled.push(tx.local_uuid || tx.localUuid);
        }
        
        await client.query('COMMIT');
        res.json({ success: true, reconciled });
        
    } catch (error) {
        await client.query('ROLLBACK');
        console.error('Reconciliation error:', error);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    } finally {
        client.release();
    }
});

app.post('/api/v1/production/run', async (req, res) => {
    const { tenant_id, item_id, batch_size } = req.body;

    if (!tenant_id || !item_id || !batch_size || batch_size <= 0) {
        return res.status(400).json({ success: false, error: 'Invalid payload' });
    }

    const client = await pool.connect();

    try {
        await client.query('BEGIN');

        // Fetch the finished good
        const fgRes = await client.query(
            'SELECT * FROM inventory_items WHERE id = $1 AND tenant_id = $2 AND classification = $3 FOR UPDATE',
            [item_id, tenant_id, 'FINISHED_GOOD']
        );

        if (fgRes.rows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({ success: false, error: 'Finished good not found' });
        }

        const fg = fgRes.rows[0];
        const recipe = fg.recipe_configuration_jsonb;

        if (recipe && Array.isArray(recipe)) {
            // Loop through recipe to decrement raw materials
            for (const reqItem of recipe) {
                const { raw_material_id, quantity_per_unit } = reqItem;
                const totalRequired = quantity_per_unit * batch_size;

                const rmRes = await client.query(
                    'SELECT current_stock_warehouse FROM inventory_items WHERE id = $1 AND tenant_id = $2 AND classification = $3 FOR UPDATE',
                    [raw_material_id, tenant_id, 'RAW_MATERIAL']
                );

                if (rmRes.rows.length === 0) {
                    await client.query('ROLLBACK');
                    return res.status(400).json({ success: false, error: `Raw material not found: ${raw_material_id}` });
                }

                const rm = rmRes.rows[0];

                if (rm.current_stock_warehouse < totalRequired) {
                    await client.query('ROLLBACK');
                    return res.status(400).json({ success: false, error: 'Insufficient Raw Materials' });
                }

                // Decrement stock
                await client.query(
                    'UPDATE inventory_items SET current_stock_warehouse = current_stock_warehouse - $1 WHERE id = $2',
                    [totalRequired, raw_material_id]
                );
            }
        }

        // Increment finished good stock
        await client.query(
            'UPDATE inventory_items SET current_stock_warehouse = current_stock_warehouse + $1 WHERE id = $2',
            [batch_size, item_id]
        );

        await client.query('COMMIT');
        res.json({ success: true, message: 'Production run logged successfully' });

    } catch (error) {
        await client.query('ROLLBACK');
        console.error('Production run error:', error);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    } finally {
        client.release();
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});
