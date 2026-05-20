CREATE TABLE withholding_tax_tables (
    id BIGSERIAL PRIMARY KEY,
    tax_year INTEGER NOT NULL,
    name VARCHAR(120) NOT NULL,
    source VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tax_year, name)
);

CREATE TABLE withholding_tax_brackets (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL REFERENCES withholding_tax_tables(id) ON DELETE CASCADE,
    salary_from BIGINT NOT NULL,
    salary_to BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (table_id, salary_from, salary_to)
);

CREATE TABLE withholding_tax_amounts (
    id BIGSERIAL PRIMARY KEY,
    bracket_id BIGINT NOT NULL REFERENCES withholding_tax_brackets(id) ON DELETE CASCADE,
    dependent_count INTEGER NOT NULL,
    monthly_income_tax BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (bracket_id, dependent_count)
);

CREATE TABLE expense_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    parent_category_id BIGINT REFERENCES expense_categories(id),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    category_id BIGINT NOT NULL REFERENCES expense_categories(id),
    merchant_name VARCHAR(160) NOT NULL,
    merchant_category_code VARCHAR(20),
    payment_method VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    approved_at TIMESTAMPTZ NOT NULL,
    external_order_id VARCHAR(120),
    source VARCHAR(40) NOT NULL DEFAULT 'APP',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_transactions_user_time ON user_transactions(user_id, approved_at DESC);
CREATE INDEX idx_user_transactions_category ON user_transactions(category_id);

CREATE TABLE tax_computation_parameters (
    id BIGSERIAL PRIMARY KEY,
    tax_year INTEGER NOT NULL,
    parameter_key VARCHAR(80) NOT NULL,
    parameter_value NUMERIC(18, 6) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tax_year, parameter_key)
);

CREATE TABLE deduction_category_rules (
    id BIGSERIAL PRIMARY KEY,
    tax_year INTEGER NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    deduction_rate NUMERIC(8, 6) NOT NULL,
    annual_limit BIGINT,
    threshold_fill_priority INTEGER NOT NULL,
    applies_to_card_threshold BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tax_year, category_code)
);

CREATE INDEX idx_deduction_category_rules_year_priority
    ON deduction_category_rules(tax_year, threshold_fill_priority);
