ALTER TABLE user_transactions
    ADD COLUMN external_tx_id VARCHAR(120);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_transactions_source_external_tx_id
    ON user_transactions(user_id, source, external_tx_id)
    WHERE external_tx_id IS NOT NULL;

CREATE TABLE external_finance_links (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    provider VARCHAR(30) NOT NULL,
    organization VARCHAR(40) NOT NULL,
    connected_id VARCHAR(120) NOT NULL,
    client_type VARCHAR(30) NOT NULL DEFAULT 'PERSONAL',
    account_ref VARCHAR(120),
    metadata JSONB,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, provider, connected_id)
);

CREATE INDEX idx_external_finance_links_user_provider
    ON external_finance_links(user_id, provider, active);

CREATE TABLE transaction_import_runs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    provider VARCHAR(30) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    imported_count INTEGER NOT NULL DEFAULT 0,
    skipped_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    message TEXT
);

INSERT INTO expense_categories (code, name, description)
VALUES
    ('CASH_RECEIPT', '현금영수증', '현금영수증 사용액'),
    ('NON_DEDUCTIBLE', '공제제외', '공제 제외 항목')
ON CONFLICT (code) DO NOTHING;

INSERT INTO deduction_category_rules (
    tax_year,
    category_code,
    deduction_rate,
    annual_limit,
    threshold_fill_priority,
    applies_to_card_threshold
)
VALUES
    (2026, 'CASH_RECEIPT', 0.300000, 3000000, 2, true),
    (2026, 'NON_DEDUCTIBLE', 0.000000, 0, 100, false)
ON CONFLICT (tax_year, category_code) DO NOTHING;
