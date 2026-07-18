async function fetchDashboardData() {
    const loader = document.getElementById('loader');
    const content = document.getElementById('dashboard-content');
    const errorState = document.getElementById('error-state');
    const errorMessage = document.getElementById('error-message');

    try {
        const response = await fetch('/api/v1/dashboard/summary?tenant_id=b1234567-89ab-cdef-0123-456789abcdef');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success && result.data) {
            // Update DOM elements
            document.getElementById('metric-customers').textContent = result.data.total_customers.toLocaleString();
            document.getElementById('metric-receivable').textContent = result.data.total_receivable.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            document.getElementById('metric-verified').textContent = result.data.verified_visits.toLocaleString();
            document.getElementById('metric-unverified').textContent = result.data.unverified_visits.toLocaleString();
            
            // Show content, hide loader
            loader.classList.add('hidden');
            content.classList.remove('hidden');
        } else {
            throw new Error(result.error || 'Failed to fetch data');
        }
        
    } catch (error) {
        console.error('Error fetching dashboard data:', error);
        // Show error state, hide loader
        loader.classList.add('hidden');
        errorState.classList.remove('hidden');
        errorMessage.textContent = error.message || 'Unable to connect to the server.';
    }
}

// Initialize on load
document.addEventListener('DOMContentLoaded', fetchDashboardData);

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
            let invoiceHTML = `================================
           POURIFY
   Combined Logistics Invoice
================================
`;
            invoiceHTML += `Date: ${new Date().toLocaleString()}
`;
            invoiceHTML += `Target Client: ${businessName}
`;
            invoiceHTML += `--------------------------------
`;
            
            let totalCharged = 0;
            let totalCollected = 0;
            let totalDelivered = 0;
            
            if (txs.length === 0) {
                invoiceHTML += `No recent transactions found.
`;
            } else {
                txs.forEach(tx => {
                    const dateStr = new Date(parseInt(tx.epoch_timestamp)).toLocaleString();
                    invoiceHTML += `Date: ${dateStr}
`;
                    invoiceHTML += `Agent: ${tx.driver_name || 'Unknown'}
`;
                    invoiceHTML += `Type: ${tx.record_classification}
`;
                    if (tx.item_units_delivered > 0) invoiceHTML += `Delivered: ${tx.item_units_delivered} Units
`;
                    if (tx.amount_charged > 0) invoiceHTML += `Charged: PKR ${tx.amount_charged}
`;
                    if (tx.amount_collected > 0) invoiceHTML += `Paid: PKR ${tx.amount_collected}
`;
                    invoiceHTML += `--------------------------------
`;
                    
                    totalCharged += parseFloat(tx.amount_charged || 0);
                    totalCollected += parseFloat(tx.amount_collected || 0);
                    totalDelivered += parseInt(tx.item_units_delivered || 0);
                });
            }
            
            invoiceHTML += `
SUMMARY:
`;
            invoiceHTML += `Total Units Delivered: ${totalDelivered}
`;
            invoiceHTML += `Total Charged: PKR ${totalCharged}
`;
            invoiceHTML += `Total Paid: PKR ${totalCollected}
`;
            invoiceHTML += `================================
`;
            
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
