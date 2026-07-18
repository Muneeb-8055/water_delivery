const express = require('express');
const { Pool } = require('pg');

const app = express();
app.use(express.json());
app.use(express.static(\public\));

const pool = new Pool({
  connectionString: process.env.DATABASE_URL
});

// Haversine formula for Proximity Auditing
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // metres
    const radLat1 = lat1 * Math.PI/180;
    const radLat2 = lat2 * Math.PI/180;
    const deltaLat = (lat2-lat1) * Math.PI/180;
    const deltaLon = (lon2-lon1) * Math.PI/180;

    const a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
            Math.cos(radLat1) * Math.cos(radLat2) *
            Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c; // in metres
}

// 2. Role-Based Access Control (RBAC) Middleware
const checkRights = (requiredRight) => {
    return async (req, res, next) => {
        const { user_id, tenant_id } = req.headers; // Assuming auth info is passed in headers for simplicity

        if (!user_id || !tenant_id) {
            return res.status(401).json({ success: false, error: 'Unauthorized' });
        }

        try {
            const userRes = await pool.query(`
                SELECT r.allowed_rights 
                FROM users u
                JOIN roles_and_rights r ON u.role_id = r.role_id
                WHERE u.user_id = $1 AND u.tenant_id = $2
            `, [user_id, tenant_id]);

            if (userRes.rows.length === 0) {
                return res.status(403).json({ success: false, error: 'Forbidden' });
            }

            const allowedRights = userRes.rows[0].allowed_rights || [];
            
            if (allowedRights.includes(requiredRight)) {
                req.tenant_id = tenant_id;
                req.user_id = user_id;
                next();
            } else {
                return res.status(403).json({ success: false, error: 'Forbidden: Missing required right' });
            }
        } catch (error) {
            console.error('RBAC Error:', error);
            res.status(500).json({ success: false, error: 'Internal Server Error' });
        }
    };
};

// Phase 4: The Reconcile Endpoint (Idempotent API)
app.post('/api/v1/mobile/itinerary/reconcile', async (req, res) => {
    const { tenant_id, driver_id, transactions, gps_logs } = req.body;

    if (!tenant_id || !driver_id) {
        return res.status(400).json({ success: false, error: 'Missing tenant_id or driver_id' });
    }

    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        // 1. Process GPS Logs
        if (gps_logs && gps_logs.length > 0) {
            for (const log of gps_logs) {
                await client.query(`
                    INSERT INTO gps_track_logs (local_uuid, tenant_id, driver_id, latitude, longitude, epoch_timestamp)
                    VALUES ($1, $2, $3, $4, $5, $6)
                    ON CONFLICT (local_uuid) DO NOTHING
                `, [log.local_uuid, tenant_id, driver_id, log.latitude, log.longitude, log.epochTimestamp]);
            }
        }

        // 2. Process Transactions Idempotently with Haversine Geo-Auditing
        const reconciledIds = [];
        if (transactions && transactions.length > 0) {
            for (const tx of transactions) {
                // Check if already processed (Idempotency)
                const existing = await client.query('SELECT local_uuid FROM transaction_ledger WHERE local_uuid = $1', [tx.localUuid]);
                if (existing.rows.length > 0) {
                    reconciledIds.push(tx.localUuid);
                    continue; 
                }

                // Haversine Validation against Master Customer Coordinates
                let proximityStatus = 'UNVERIFIED';
                const customerRes = await client.query('SELECT geo_latitude, geo_longitude FROM customers WHERE customer_id = $1 AND tenant_id = $2', [tx.customerId, tenant_id]);
                
                if (customerRes.rows.length > 0 && tx.latitude && tx.longitude) {
                    const cust = customerRes.rows[0];
                    if (cust.geo_latitude && cust.geo_longitude) {
                        const distance = calculateDistance(tx.latitude, tx.longitude, cust.geo_latitude, cust.geo_longitude);
                        if (distance <= 50) {
                            proximityStatus = 'VERIFIED';
                        }
                    }
                }

                // Insert into Ledger
                await client.query(`
                    INSERT INTO transaction_ledger (
                        local_uuid, tenant_id, customer_id, record_classification, amount_charged,
                        amount_collected, item_units_delivered, package_assets_recovered,
                        epoch_timestamp, sync_state, proximity_status
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, 'SYNCED', $10)
                `, [
                    tx.localUuid, tenant_id, tx.customerId, tx.recordClassification,
                    tx.amountCharged, tx.amountCollected, tx.itemUnitsDelivered,
                    tx.packageAssetsRecovered, tx.epochTimestamp, proximityStatus
                ]);
                
                // Double-Entry Accounting / Balance Updates
                if (tx.recordClassification === 'SALE') {
                    await client.query(`
                        UPDATE customers 
                        SET balance_receivable = balance_receivable + $1 - $2,
                            company_owned_bottles = company_owned_bottles + $3 - $4
                        WHERE customer_id = $5 AND tenant_id = $6
                    `, [tx.amountCharged, tx.amountCollected, tx.itemUnitsDelivered, tx.packageAssetsRecovered, tx.customerId, tenant_id]);
                } else if (tx.recordClassification === 'RECOVERY') {
                    await client.query(`
                        UPDATE customers 
                        SET balance_receivable = balance_receivable - $1
                        WHERE customer_id = $2 AND tenant_id = $3
                    `, [tx.amountCollected, tx.customerId, tenant_id]);
                }

                reconciledIds.push(tx.localUuid);
            }
        }

        await client.query('COMMIT');
        res.json({ success: true, reconciled: reconciledIds });
    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Reconciliation error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    } finally {
        client.release();
    }
});

// Phase 5: Filter Plant (Bill of Materials) Logic
app.post('/api/v1/production/run', async (req, res) => {
    const { tenant_id, finished_good_id, batch_size } = req.body;

    if (!tenant_id || !finished_good_id || !batch_size || batch_size <= 0) {
        return res.status(400).json({ success: false, error: 'Invalid parameters' });
    }

    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        // Fetch finished good details
        const fgRes = await client.query('SELECT current_stock_warehouse, recipe_configuration FROM inventory_items WHERE item_id = $1 AND tenant_id = $2 AND classification = $3 FOR UPDATE', [finished_good_id, tenant_id, 'FINISHED_GOOD']);
        
        if (fgRes.rows.length === 0) {
            await client.query('ROLLBACK');
            return res.status(404).json({ success: false, error: 'Finished good not found' });
        }

        const recipe = fgRes.rows[0].recipe_configuration || [];

        // Check raw material stock availability
        for (const ingredient of recipe) {
            const requiredQty = ingredient.quantity_per_unit * batch_size;
            const rmRes = await client.query('SELECT current_stock_warehouse FROM inventory_items WHERE item_id = $1 AND tenant_id = $2 FOR UPDATE', [ingredient.raw_material_id, tenant_id]);
            
            if (rmRes.rows.length === 0) {
                await client.query('ROLLBACK');
                return res.status(400).json({ success: false, error: `Raw material ${ingredient.raw_material_id} not found` });
            }

            if (rmRes.rows[0].current_stock_warehouse < requiredQty) {
                await client.query('ROLLBACK');
                return res.status(400).json({ success: false, error: `Insufficient stock for raw material ${ingredient.raw_material_id}` });
            }
        }

        // Deduct raw materials
        for (const ingredient of recipe) {
            const requiredQty = ingredient.quantity_per_unit * batch_size;
            await client.query('UPDATE inventory_items SET current_stock_warehouse = current_stock_warehouse - $1 WHERE item_id = $2 AND tenant_id = $3', [requiredQty, ingredient.raw_material_id, tenant_id]);
        }

        // Increment finished good stock
        await client.query('UPDATE inventory_items SET current_stock_warehouse = current_stock_warehouse + $1 WHERE item_id = $2 AND tenant_id = $3', [batch_size, finished_good_id, tenant_id]);

        await client.query('COMMIT');
        res.json({ success: true, message: 'Production run logged successfully' });

    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Production Error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    } finally {
        client.release();
    }
});

// Phase 5: Single Unified Dashboard API Route
app.get('/api/v1/dashboard/summary', async (req, res) => {
    // Assuming tenant_id is passed in query for now
    const { tenant_id } = req.query;

    if (!tenant_id) {
        return res.status(400).json({ success: false, error: 'Missing tenant_id' });
    }

    try {
        const customersRes = await pool.query('SELECT COUNT(*) as total_customers, SUM(balance_receivable) as total_receivable FROM customers WHERE tenant_id = $1', [tenant_id]);
        
        // Count today's visits
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const epochToday = todayStart.getTime();

        const ledgerRes = await pool.query(`
            SELECT 
                COUNT(*) FILTER (WHERE proximity_status = 'VERIFIED') as verified_visits,
                COUNT(*) FILTER (WHERE proximity_status = 'UNVERIFIED') as unverified_visits
            FROM transaction_ledger 
            WHERE tenant_id = $1 AND epoch_timestamp >= $2
        `, [tenant_id, epochToday]);

        res.json({
            success: true,
            data: {
                total_customers: parseInt(customersRes.rows[0].total_customers) || 0,
                total_receivable: parseFloat(customersRes.rows[0].total_receivable) || 0,
                verified_visits: parseInt(ledgerRes.rows[0].verified_visits) || 0,
                unverified_visits: parseInt(ledgerRes.rows[0].unverified_visits) || 0
            }
        });
    } catch (err) {
        console.error('Dashboard Summary Error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Tarsil Backend listening on port ${PORT}`);
});
