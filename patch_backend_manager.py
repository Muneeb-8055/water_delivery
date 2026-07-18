import re

with open("backend/schema.sql", "r") as f:
    schema = f.read()

if "driver_id UUID REFERENCES users(user_id)" not in schema:
    schema = schema.replace(
        "tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),",
        "tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),\n    driver_id UUID REFERENCES users(user_id),"
    )
    with open("backend/schema.sql", "w") as f:
        f.write(schema)


with open("backend/index.js", "r") as f:
    index_js = f.read()

# Update transaction_ledger insert to include driver_id
if "driver_id," not in index_js.split("INSERT INTO transaction_ledger (")[1].split(")")[0]:
    old_insert = """INSERT INTO transaction_ledger (
                        local_uuid, tenant_id, customer_id, record_classification, amount_charged,
                        amount_collected, item_units_delivered, package_assets_recovered,
                        epoch_timestamp, sync_state, proximity_status
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, 'SYNCED', $10)"""
    new_insert = """INSERT INTO transaction_ledger (
                        local_uuid, tenant_id, driver_id, customer_id, record_classification, amount_charged,
                        amount_collected, item_units_delivered, package_assets_recovered,
                        epoch_timestamp, sync_state, proximity_status
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'SYNCED', $11)"""
    
    index_js = index_js.replace(old_insert, new_insert)
    
    old_values = """tx.localUuid, tenant_id, tx.customerId, tx.recordClassification,
                    tx.amountCharged, tx.amountCollected, tx.itemUnitsDelivered,
                    tx.packageAssetsRecovered, tx.epochTimestamp, proximityStatus"""
    new_values = """tx.localUuid, tenant_id, driver_id, tx.customerId, tx.recordClassification,
                    tx.amountCharged, tx.amountCollected, tx.itemUnitsDelivered,
                    tx.packageAssetsRecovered, tx.epochTimestamp, proximityStatus"""
    
    index_js = index_js.replace(old_values, new_values)

# Add manager endpoints
manager_endpoints = """
// --- MANAGER PORTAL ENDPOINTS ---

// Get agents and their progress
app.get('/api/v1/manager/agents', async (req, res) => {
    const { tenant_id } = req.query;
    if (!tenant_id) return res.status(400).json({ success: false, error: 'Missing tenant_id' });
    
    try {
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const epochToday = todayStart.getTime();

        const agentsRes = await pool.query(`
            SELECT u.user_id, u.username, 
                   COUNT(t.local_uuid) as deliveries_today,
                   SUM(t.amount_collected) as cash_collected_today
            FROM users u
            LEFT JOIN transaction_ledger t ON u.user_id = t.driver_id AND t.epoch_timestamp >= $2
            WHERE u.tenant_id = $1 AND u.role_id IS NULL -- Simplified for agents without specific roles
            GROUP BY u.user_id, u.username
        `, [tenant_id, epochToday]);
        
        res.json({ success: true, data: agentsRes.rows });
    } catch (err) {
        console.error('Agents progress error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    }
});

// Create agent
app.post('/api/v1/manager/agents', async (req, res) => {
    const { tenant_id, username, password_hash } = req.body;
    if (!tenant_id || !username) return res.status(400).json({ success: false, error: 'Missing params' });
    
    try {
        const insertRes = await pool.query(`
            INSERT INTO users (tenant_id, username) VALUES ($1, $2) RETURNING user_id
        `, [tenant_id, username]);
        res.json({ success: true, user_id: insertRes.rows[0].user_id });
    } catch (err) {
        console.error('Create agent error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    }
});

// Get customers for combined invoice
app.get('/api/v1/manager/customers', async (req, res) => {
    const { tenant_id } = req.query;
    if (!tenant_id) return res.status(400).json({ success: false, error: 'Missing tenant_id' });
    
    try {
        const customersRes = await pool.query(`
            SELECT customer_id, business_name, balance_receivable, contact_phone
            FROM customers WHERE tenant_id = $1
        `, [tenant_id]);
        res.json({ success: true, data: customersRes.rows });
    } catch (err) {
        console.error('Get customers error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    }
});

// Get customer transactions
app.get('/api/v1/manager/customers/:customer_id/transactions', async (req, res) => {
    const { tenant_id } = req.query;
    const { customer_id } = req.params;
    
    try {
        const txRes = await pool.query(`
            SELECT t.*, u.username as driver_name
            FROM transaction_ledger t
            LEFT JOIN users u ON t.driver_id = u.user_id
            WHERE t.tenant_id = $1 AND t.customer_id = $2
            ORDER BY t.epoch_timestamp DESC
        `, [tenant_id, customer_id]);
        res.json({ success: true, data: txRes.rows });
    } catch (err) {
        console.error('Get transactions error:', err);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    }
});
"""

if "/api/v1/manager/agents" not in index_js:
    index_js = index_js.replace("const PORT = process.env.PORT || 3000;", manager_endpoints + "\nconst PORT = process.env.PORT || 3000;")
    with open("backend/index.js", "w") as f:
        f.write(index_js)
    print("Patched backend successfully")
else:
    print("Already patched")
