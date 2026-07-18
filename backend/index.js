const express = require('express');
const { Pool } = require('pg');

const app = express();
app.use(express.json());

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

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Tarsil Backend listening on port ${PORT}`);
});
