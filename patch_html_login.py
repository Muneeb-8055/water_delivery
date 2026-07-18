import re

with open("backend/public/index.html", "r") as f:
    html = f.read()

login_view_html = """
    <!-- Login View -->
    <div id="view-login" class="min-h-screen flex items-center justify-center bg-slate-100">
        <div class="bg-white p-8 rounded-lg shadow-md w-96">
            <h2 class="text-2xl font-bold text-center mb-6 text-brand-dark">Manager Login</h2>
            <form id="login-form" onsubmit="handleLogin(event)">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-slate-700 mb-1">Username</label>
                    <input type="text" id="login-username" required class="w-full border border-slate-300 rounded px-3 py-2" placeholder="admin">
                </div>
                <div class="mb-6">
                    <label class="block text-sm font-medium text-slate-700 mb-1">Password</label>
                    <input type="password" id="login-password" required class="w-full border border-slate-300 rounded px-3 py-2" placeholder="admin">
                </div>
                <button type="submit" class="w-full bg-brand-teal text-white py-2 rounded font-medium hover:bg-teal-600">Login</button>
                <p id="login-error" class="text-red-500 text-sm mt-4 hidden text-center">Invalid credentials</p>
                <div class="mt-4 text-center text-xs text-slate-500">
                    <p>Demo Credentials:</p>
                    <p>User: <strong>admin</strong> | Pass: <strong>admin</strong></p>
                </div>
            </form>
        </div>
    </div>

    <!-- Dashboard Content Wrapper (Initially hidden) -->
    <div id="dashboard-wrapper" class="hidden">
"""

# Find <nav class="bg-brand-dark text-white shadow-lg"> and insert login view right before it, wrapping the rest.
# Or better, just insert after <body> and wrap the rest.

html = html.replace("<body>\n    <nav", "<body>\n" + login_view_html + "    <nav")

# Close the wrapper before the scripts
html = html.replace("\n    <!-- Invoice Modal -->", "\n    </div> <!-- End Dashboard Wrapper -->\n    <!-- Invoice Modal -->")

js_login = """
        let tenantId = ''; // No longer hardcoded

        async function handleLogin(e) {
            e.preventDefault();
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;
            const errorEl = document.getElementById('login-error');
            
            try {
                const res = await fetch('/api/v1/manager/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });
                const json = await res.json();
                
                if (json.success) {
                    tenantId = json.tenant_id;
                    errorEl.classList.add('hidden');
                    
                    // Hide login, show dashboard
                    document.getElementById('view-login').classList.add('hidden');
                    document.getElementById('dashboard-wrapper').classList.remove('hidden');
                    
                    // Init dashboard data
                    switchTab('overview');
                } else {
                    errorEl.classList.remove('hidden');
                }
            } catch (err) {
                console.error(err);
                errorEl.textContent = 'Server error';
                errorEl.classList.remove('hidden');
            }
        }
"""

html = html.replace("const tenantId = 'b1234567-89ab-cdef-0123-456789abcdef';", js_login)

# Remove the initial fetchSummary() call and setInterval at the bottom, so it only starts after login.
# We will do this via a regex or simple replace
html = html.replace("fetchSummary();\n        setInterval(() => {", "// fetchSummary(); // Replaced with logic inside switchTab\n        setInterval(() => {")

with open("backend/public/index.html", "w") as f:
    f.write(html)
    print("Patched HTML login")

