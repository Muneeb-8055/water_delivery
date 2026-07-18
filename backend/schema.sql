CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tenants (
    tenant_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
    customer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    business_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    geo_latitude DOUBLE PRECISION,
    geo_longitude DOUBLE PRECISION,
    historical_rate NUMERIC(10, 2),
    balance_receivable NUMERIC(15, 2) DEFAULT 0,
    company_owned_bottles INT DEFAULT 0,
    deposit_backed_bottles INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_items (
    item_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    title VARCHAR(255) NOT NULL,
    classification VARCHAR(50) NOT NULL,
    base_rate NUMERIC(10, 2) NOT NULL,
    stock_level INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction_ledger (
    local_uuid UUID PRIMARY KEY, -- Client generated UUID
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    customer_id UUID NOT NULL REFERENCES customers(customer_id),
    record_classification VARCHAR(50) NOT NULL, -- 'SALE', 'RECOVERY', 'SKIP'
    amount_charged NUMERIC(15, 2) DEFAULT 0,
    amount_collected NUMERIC(15, 2) DEFAULT 0,
    item_units_delivered INT DEFAULT 0,
    package_assets_recovered INT DEFAULT 0,
    epoch_timestamp BIGINT NOT NULL,
    sync_state VARCHAR(50) DEFAULT 'SYNCED',
    proximity_status VARCHAR(50) -- 'VERIFIED', 'UNVERIFIED'
);

CREATE TABLE gps_track_logs (
    local_uuid UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    driver_id UUID NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    epoch_timestamp BIGINT NOT NULL
);
