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
