CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    business_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    geo_latitude DOUBLE PRECISION,
    geo_longitude DOUBLE PRECISION,
    receivable_balance NUMERIC(10, 2) DEFAULT 0,
    company_owned_bottles INT DEFAULT 0
);

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    title VARCHAR(255) NOT NULL,
    classification VARCHAR(50) NOT NULL, -- 'FINISHED_GOOD', 'RAW_MATERIAL'
    current_stock_warehouse INT DEFAULT 0,
    recipe_configuration_jsonb JSONB
);

CREATE TABLE transaction_ledger (
    tx_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    local_uuid VARCHAR(255) UNIQUE NOT NULL,
    customer_id UUID REFERENCES customers(id),
    record_classification VARCHAR(50) NOT NULL, -- 'SALE', 'RECOVERY', 'SKIP'
    amount_charged NUMERIC(10, 2) DEFAULT 0,
    amount_collected NUMERIC(10, 2) DEFAULT 0,
    visit_validation_state VARCHAR(50), -- 'VERIFIED', 'UNVERIFIED'
    epoch_timestamp BIGINT NOT NULL
);
