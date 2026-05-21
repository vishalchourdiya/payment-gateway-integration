CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    mid VARCHAR(50) NOT NULL,
    order_no VARCHAR(35) NOT NULL UNIQUE,
    amount NUMERIC(13,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    user_id VARCHAR(30),
    customer_vpa VARCHAR(50),
    mobile_no VARCHAR(13),
    email_id VARCHAR(50),
    customer_name VARCHAR(100),
    txn_id VARCHAR(40) UNIQUE,
    cust_ref_no VARCHAR(40),
    txn_resp_code VARCHAR(20),
    provider_txn_status VARCHAR(30),
    qr_string TEXT,
    provider_raw_response TEXT,
    last_callback_payload TEXT,
    last_callback_signature VARCHAR(80),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_payment_order_no ON payment_transactions(order_no);
CREATE INDEX IF NOT EXISTS idx_payment_txn_id ON payment_transactions(txn_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_transactions(status);
CREATE INDEX IF NOT EXISTS idx_payment_created_at ON payment_transactions(created_at);

CREATE TABLE IF NOT EXISTS merchants (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) NOT NULL UNIQUE,
    merchant_name VARCHAR(120) NOT NULL,
    encryption_key VARCHAR(64) NOT NULL,
    callback_url VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_merchant_mid ON merchants(mid);

CREATE TABLE IF NOT EXISTS transaction_audits (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(35) NOT NULL,
    old_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    source_ip VARCHAR(45),
    raw_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_audit_order_no ON transaction_audits(order_no);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON transaction_audits(created_at);

CREATE TABLE IF NOT EXISTS callback_events (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(35),
    signature VARCHAR(80) NOT NULL UNIQUE,
    encrypted_data TEXT NOT NULL,
    decrypted_data TEXT,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason VARCHAR(255),
    received_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_callback_signature ON callback_events(signature);
CREATE INDEX IF NOT EXISTS idx_callback_order_no ON callback_events(order_no);

INSERT INTO merchants(mid, merchant_name, encryption_key, callback_url, active, created_at)
VALUES ('DEMO_MERCHANT_001', 'Default Demo Merchant', '0123456789abcdef0123456789abcdef', 'http://localhost:8080/api/v1/payments/callback', true, NOW())
ON CONFLICT (mid) DO NOTHING;
