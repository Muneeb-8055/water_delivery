with open("backend/public/index.html", "r") as f:
    html = f.read()

# Add a Create Customer form right above the Customer Balances table
form_html = """
            <!-- Create Customer -->
            <div class="bg-white rounded-lg shadow border border-slate-200 mb-8 p-6">
                <h3 class="text-lg font-bold text-brand-dark mb-4">Add New Customer</h3>
                <form id="add-customer-form" onsubmit="createCustomer(event)" class="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1">Business Name</label>
                        <input type="text" id="cust-name" required class="w-full border border-slate-300 rounded px-3 py-2">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1">Phone</label>
                        <input type="text" id="cust-phone" required class="w-full border border-slate-300 rounded px-3 py-2">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1">Starting Balance</label>
                        <input type="number" id="cust-balance" value="0" class="w-full border border-slate-300 rounded px-3 py-2">
                    </div>
                    <div class="flex items-end">
                        <button type="submit" class="w-full bg-brand-blue text-white py-2 rounded font-medium hover:bg-blue-600">Create Customer</button>
                    </div>
                </form>
            </div>
            
"""

html = html.replace("<!-- Customer Balances & Invoicing -->", "<!-- Customer Balances & Invoicing -->\n" + form_html)

js = """
        async function createCustomer(e) {
            e.preventDefault();
            const name = document.getElementById('cust-name').value;
            const phone = document.getElementById('cust-phone').value;
            const balance = document.getElementById('cust-balance').value;
            try {
                const res = await fetch('/api/v1/manager/customers', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        tenant_id: tenantId,
                        business_name: name,
                        contact_phone: phone,
                        balance_receivable: parseFloat(balance || 0),
                        visit_status: 'SCHEDULED'
                    })
                });
                const json = await res.json();
                if (json.success) {
                    document.getElementById('add-customer-form').reset();
                    loadCustomers();
                } else {
                    alert('Failed: ' + json.error);
                }
            } catch (err) { console.error(err); }
        }
"""

html = html.replace("async function loadCustomers() {", js + "\n        async function loadCustomers() {")

with open("backend/public/index.html", "w") as f:
    f.write(html)
