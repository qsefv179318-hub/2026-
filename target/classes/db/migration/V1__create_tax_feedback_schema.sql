CREATE TABLE users (
    id UUID PRIMARY KEY,
    annual_income BIGINT NOT NULL,
    dependents_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tax_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tax_rules (
    id BIGSERIAL PRIMARY KEY,
    tax_year INTEGER NOT NULL,
    category_id BIGINT NOT NULL REFERENCES tax_categories(id),
    deduction_rate NUMERIC(5, 4) NOT NULL,
    base_limit BIGINT NOT NULL DEFAULT 0,
    additional_limit BIGINT NOT NULL DEFAULT 0,
    min_income_threshold_rate NUMERIC(5, 4) NOT NULL DEFAULT 0.2500,
    income_limit BIGINT,
    source VARCHAR(80) NOT NULL DEFAULT 'MANUAL_SEED',
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tax_year, category_id)
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    toss_order_id VARCHAR(120) UNIQUE,
    payment_key VARCHAR(200),
    merchant_name VARCHAR(160) NOT NULL,
    merchant_category_code VARCHAR(20),
    amount BIGINT NOT NULL,
    method VARCHAR(30) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES tax_categories(id),
    is_deductible BOOLEAN NOT NULL DEFAULT true,
    approved_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_user_approved_at ON payments(user_id, approved_at DESC);
CREATE INDEX idx_payments_category ON payments(category_id);

CREATE TABLE feedbacks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    payment_id UUID REFERENCES payments(id),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,
    expected_benefit BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_feedbacks_user_created_at ON feedbacks(user_id, created_at DESC);

CREATE TABLE tax_rule_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(80) NOT NULL,
    tax_year INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    message TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
