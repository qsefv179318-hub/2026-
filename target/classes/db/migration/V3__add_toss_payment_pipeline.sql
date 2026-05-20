CREATE TABLE payment_orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    order_id VARCHAR(64) NOT NULL UNIQUE,
    order_name VARCHAR(160) NOT NULL,
    amount BIGINT NOT NULL,
    merchant_name VARCHAR(160) NOT NULL,
    merchant_category_code VARCHAR(20),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_orders_user_created_at ON payment_orders(user_id, created_at DESC);

ALTER TABLE payments
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'DONE',
    ADD COLUMN raw_payload JSONB;

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_payment_key UNIQUE (payment_key);

CREATE TABLE toss_webhook_events (
    id UUID PRIMARY KEY,
    event_id VARCHAR(120) UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    payment_key VARCHAR(200),
    order_id VARCHAR(120),
    status VARCHAR(40),
    raw_payload JSONB NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT false,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_toss_webhook_events_order_id ON toss_webhook_events(order_id);
CREATE INDEX idx_toss_webhook_events_payment_key ON toss_webhook_events(payment_key);
