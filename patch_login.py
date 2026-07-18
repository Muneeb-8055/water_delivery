with open("backend/index.js", "r") as f:
    content = f.read()

login_endpoint = """
// Manager Login Endpoint
app.post('/api/v1/manager/login', (req, res) => {
    const { username, password } = req.body;
    // For demo purposes, we are using hardcoded credentials for the manager.
    // In production, you would hash the password and check against the users table.
    if (username === 'admin' && password === 'admin') {
        // Return the demo tenant_id
        res.json({ success: true, tenant_id: 'b1234567-89ab-cdef-0123-456789abcdef' });
    } else {
        res.status(401).json({ success: false, error: 'Invalid credentials' });
    }
});
"""

if "/api/v1/manager/login" not in content:
    content = content.replace("// --- MANAGER PORTAL ENDPOINTS ---", "// --- MANAGER PORTAL ENDPOINTS ---\n" + login_endpoint)
    with open("backend/index.js", "w") as f:
        f.write(content)
        print("Patched backend")
