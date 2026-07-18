with open("backend/index.js", "r") as f:
    content = f.read()

endpoint = """
// Add customer
app.post('/api/v1/manager/customers', async (req, res) => {
    const { tenant_id, business_name, contact_phone, balance_receivable, visit_status } = req.body;
    if (!tenant_id || !business_name) return res.status(400).json({ success: false, error: 'Missing fields' });

    try {
        const id = require('crypto').randomUUID();
        const result = await pool.query(
            `INSERT INTO customers (id, tenant_id, business_name, contact_phone, geo_latitude, geo_longitude, balance_receivable, company_owned_bottles, visit_status)
             VALUES ($1, $2, $3, $4, 0, 0, $5, 0, $6) RETURNING *`,
            [id, tenant_id, business_name, contact_phone, balance_receivable, visit_status || 'SCHEDULED']
        );
        res.json({ success: true, data: result.rows[0] });
    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, error: 'Database error' });
    }
});
"""

if "/api/v1/manager/customers" not in content and "app.post('/api/v1/manager/customers'" not in content:
    content = content.replace("app.get('/api/v1/manager/customers',", endpoint + "\napp.get('/api/v1/manager/customers',")
    with open("backend/index.js", "w") as f:
        f.write(content)
        print("Patched backend customers")
