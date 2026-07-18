const fs = require('fs');

let content = fs.readFileSync('backend/server.js', 'utf8');

// Add static serving
content = content.replace("app.use(express.json());", "app.use(express.json());\napp.use(express.static('public'));");

// Add dashboard endpoint
const dashboardEndpoint = `
app.get('/api/v1/dashboard/summary', async (req, res) => {
    const client = await pool.connect();
    try {
        const customerRes = await client.query('SELECT COUNT(*) as count, SUM(receivable_balance) as total_receivable FROM customers');
        const totalCustomers = parseInt(customerRes.rows[0].count) || 0;
        const totalReceivable = parseFloat(customerRes.rows[0].total_receivable) || 0;
        
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);
        const startOfTodayMs = startOfToday.getTime();
        
        const txRes = await client.query(
            \`SELECT visit_validation_state, COUNT(*) as count 
             FROM transaction_ledger 
             WHERE epoch_timestamp >= $1
             GROUP BY visit_validation_state\`,
            [startOfTodayMs]
        );
        
        let verifiedCount = 0;
        let unverifiedCount = 0;
        
        txRes.rows.forEach(row => {
            if (row.visit_validation_state === 'VERIFIED') verifiedCount = parseInt(row.count);
            if (row.visit_validation_state === 'UNVERIFIED') unverifiedCount = parseInt(row.count);
        });
        
        res.json({
            success: true,
            data: {
                totalCustomers,
                totalReceivable,
                todayOperations: {
                    verified: verifiedCount,
                    unverified: unverifiedCount
                }
            }
        });
    } catch (error) {
        console.error('Dashboard error:', error);
        res.status(500).json({ success: false, error: 'Internal Server Error' });
    } finally {
        client.release();
    }
});
`;

if (!content.includes('/api/v1/dashboard/summary')) {
    content = content.replace("const PORT = process.env.PORT || 3000;", dashboardEndpoint + "\nconst PORT = process.env.PORT || 3000;");
    fs.writeFileSync('backend/server.js', content);
    console.log("Patched successfully");
} else {
    console.log("Already patched");
}
