with open("backend/public/index.html", "r") as f:
    html = f.read()

tabs_html = """
    <!-- Tabs -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6">
        <div class="border-b border-slate-200">
            <nav class="-mb-px flex space-x-8" aria-label="Tabs">
                <button onclick="switchTab('dashboard')" id="tab-dashboard" class="border-brand-teal text-brand-teal whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm">
                    Overview Dashboard
                </button>
                <button onclick="switchTab('agents')" id="tab-agents" class="border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300 whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm">
                    Agents & Progress
                </button>
                <button onclick="switchTab('customers')" id="tab-customers" class="border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300 whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm">
                    Customers & Invoicing
                </button>
            </nav>
        </div>
    </div>
"""

new_sections_html = """
        <!-- Agents Section -->
        <div id="section-agents" class="hidden">
            <div class="flex justify-between items-center mb-6">
                <h2 class="text-2xl font-bold text-brand-dark">Agents Progress</h2>
                <button onclick="showCreateAgentModal()" class="bg-brand-teal text-white px-4 py-2 rounded font-medium hover:bg-teal-600">
                    + Add Agent
                </button>
            </div>
            
            <div class="bg-white rounded-lg shadow overflow-hidden border border-slate-200">
                <table class="min-w-full divide-y divide-slate-200">
                    <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Agent Name</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Deliveries Today</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Cash Collected Today</th>
                        </tr>
                    </thead>
                    <tbody id="agents-list" class="bg-white divide-y divide-slate-200">
                        <!-- Agents will be populated here -->
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Customers Section -->
        <div id="section-customers" class="hidden">
            <h2 class="text-2xl font-bold text-brand-dark mb-6">Customers & Combined Invoices</h2>
            <div class="bg-white rounded-lg shadow overflow-hidden border border-slate-200">
                <table class="min-w-full divide-y divide-slate-200">
                    <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Business Name</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Phone</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Balance Receivable</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Action</th>
                        </tr>
                    </thead>
                    <tbody id="customers-list" class="bg-white divide-y divide-slate-200">
                        <!-- Customers will be populated here -->
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Modals -->
        <div id="modal-create-agent" class="hidden fixed inset-0 bg-slate-900 bg-opacity-50 flex items-center justify-center p-4">
            <div class="bg-white rounded-lg p-6 max-w-sm w-full">
                <h3 class="text-lg font-bold mb-4">Create New Agent</h3>
                <div class="mb-4">
                    <label class="block text-sm font-medium text-slate-700 mb-1">Agent Name (Username)</label>
                    <input type="text" id="new-agent-name" class="w-full border border-slate-300 rounded px-3 py-2" placeholder="e.g. driver_khalid">
                </div>
                <div class="flex justify-end space-x-3">
                    <button onclick="closeCreateAgentModal()" class="px-4 py-2 text-slate-600 hover:text-slate-800">Cancel</button>
                    <button onclick="createAgent()" class="px-4 py-2 bg-brand-teal text-white rounded hover:bg-teal-600">Create</button>
                </div>
            </div>
        </div>

        <div id="modal-invoice" class="hidden fixed inset-0 bg-slate-900 bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div class="bg-white rounded-lg p-6 max-w-2xl w-full max-h-screen overflow-y-auto">
                <div class="flex justify-between items-start mb-4">
                    <h3 class="text-lg font-bold">Combined Invoice</h3>
                    <button onclick="closeInvoiceModal()" class="text-slate-500 hover:text-slate-700">&times;</button>
                </div>
                <div id="invoice-content" class="bg-slate-50 p-6 border border-slate-200 rounded font-mono text-sm whitespace-pre-wrap">
                    <!-- Invoice data here -->
                </div>
                <div class="mt-4 flex justify-end">
                    <button onclick="window.print()" class="px-4 py-2 bg-brand-blue text-white rounded hover:bg-blue-600">Print Invoice</button>
                </div>
            </div>
        </div>
"""

# Inject tabs
html = html.replace('<!-- Main Content -->', tabs_html + '\n    <!-- Main Content -->')

# Change dashboard content id to section-dashboard and inject new sections
html = html.replace('id="dashboard-content" class="hidden', 'id="section-dashboard" class="hidden')
html = html.replace('<!-- Dashboard Metrics -->', '<!-- Dashboard Metrics -->')

# Inject new sections before the closing main tag
html = html.replace('</main>', new_sections_html + '\n    </main>')

with open("backend/public/index.html", "w") as f:
    f.write(html)

with open("backend/public/script.js", "a") as f:
    f.write("""
const tenant_id = 'b1234567-89ab-cdef-0123-456789abcdef';

function switchTab(tabName) {
    // Hide all sections
    document.getElementById('section-dashboard').classList.add('hidden');
    document.getElementById('section-agents').classList.add('hidden');
    document.getElementById('section-customers').classList.add('hidden');
    
    // Reset tabs
    const tabs = ['dashboard', 'agents', 'customers'];
    tabs.forEach(t => {
        const el = document.getElementById('tab-' + t);
        el.className = "border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300 whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm";
    });
    
    // Show active
    document.getElementById('section-' + tabName).classList.remove('hidden');
    const activeEl = document.getElementById('tab-' + tabName);
    activeEl.className = "border-brand-teal text-brand-teal whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm";
    
    if (tabName === 'agents') loadAgents();
    if (tabName === 'customers') loadCustomers();
}

async function loadAgents() {
    try {
        const res = await fetch('/api/v1/manager/agents?tenant_id=' + tenant_id);
        const data = await res.json();
        if (data.success) {
            const tbody = document.getElementById('agents-list');
            tbody.innerHTML = '';
            data.data.forEach(agent => {
                tbody.innerHTML += `
                    <tr>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900">${agent.username}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">${agent.deliveries_today || 0}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">PKR ${agent.cash_collected_today || 0}</td>
                    </tr>
                `;
            });
        }
    } catch (e) { console.error(e); }
}

function showCreateAgentModal() { document.getElementById('modal-create-agent').classList.remove('hidden'); }
function closeCreateAgentModal() { document.getElementById('modal-create-agent').classList.add('hidden'); }

async function createAgent() {
    const name = document.getElementById('new-agent-name').value;
    if (!name) return alert('Name required');
    try {
        const res = await fetch('/api/v1/manager/agents', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ tenant_id, username: name })
        });
        const data = await res.json();
        if (data.success) {
            closeCreateAgentModal();
            loadAgents();
            document.getElementById('new-agent-name').value = '';
        } else {
            alert('Error creating agent');
        }
    } catch (e) { console.error(e); }
}

async function loadCustomers() {
    try {
        const res = await fetch('/api/v1/manager/customers?tenant_id=' + tenant_id);
        const data = await res.json();
        if (data.success) {
            const tbody = document.getElementById('customers-list');
            tbody.innerHTML = '';
            data.data.forEach(c => {
                tbody.innerHTML += `
                    <tr>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900">${c.business_name}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">${c.contact_phone || 'N/A'}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">PKR ${c.balance_receivable || 0}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <button onclick="generateCombinedInvoice('${c.customer_id}', '${c.business_name}')" class="text-brand-blue hover:text-blue-900">Generate Invoice</button>
                        </td>
                    </tr>
                `;
            });
        }
    } catch (e) { console.error(e); }
}

async function generateCombinedInvoice(customerId, businessName) {
    try {
        const res = await fetch(`/api/v1/manager/customers/${customerId}/transactions?tenant_id=` + tenant_id);
        const data = await res.json();
        if (data.success) {
            let txs = data.data;
            let invoiceHTML = `================================\n           POURIFY\n   Combined Logistics Invoice\n================================\n`;
            invoiceHTML += `Date: ${new Date().toLocaleString()}\n`;
            invoiceHTML += `Target Client: ${businessName}\n`;
            invoiceHTML += `--------------------------------\n`;
            
            let totalCharged = 0;
            let totalCollected = 0;
            let totalDelivered = 0;
            
            if (txs.length === 0) {
                invoiceHTML += `No recent transactions found.\n`;
            } else {
                txs.forEach(tx => {
                    const dateStr = new Date(parseInt(tx.epoch_timestamp)).toLocaleString();
                    invoiceHTML += `Date: ${dateStr}\n`;
                    invoiceHTML += `Agent: ${tx.driver_name || 'Unknown'}\n`;
                    invoiceHTML += `Type: ${tx.record_classification}\n`;
                    if (tx.item_units_delivered > 0) invoiceHTML += `Delivered: ${tx.item_units_delivered} Units\n`;
                    if (tx.amount_charged > 0) invoiceHTML += `Charged: PKR ${tx.amount_charged}\n`;
                    if (tx.amount_collected > 0) invoiceHTML += `Paid: PKR ${tx.amount_collected}\n`;
                    invoiceHTML += `--------------------------------\n`;
                    
                    totalCharged += parseFloat(tx.amount_charged || 0);
                    totalCollected += parseFloat(tx.amount_collected || 0);
                    totalDelivered += parseInt(tx.item_units_delivered || 0);
                });
            }
            
            invoiceHTML += `\nSUMMARY:\n`;
            invoiceHTML += `Total Units Delivered: ${totalDelivered}\n`;
            invoiceHTML += `Total Charged: PKR ${totalCharged}\n`;
            invoiceHTML += `Total Paid: PKR ${totalCollected}\n`;
            invoiceHTML += `================================\n`;
            
            document.getElementById('invoice-content').innerText = invoiceHTML;
            document.getElementById('modal-invoice').classList.remove('hidden');
        }
    } catch (e) { console.error(e); }
}

function closeInvoiceModal() { document.getElementById('modal-invoice').classList.add('hidden'); }

// override original fetchDashboardData slightly to show the tab correctly
const oldFetch = fetchDashboardData;
fetchDashboardData = async function() {
    await oldFetch();
    document.getElementById('section-dashboard').classList.remove('hidden');
    document.getElementById('dashboard-content').classList.remove('hidden');
}
""")
