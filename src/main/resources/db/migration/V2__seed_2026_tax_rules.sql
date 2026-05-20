INSERT INTO tax_categories (code, display_name, description)
VALUES
    ('CREDIT_CARD', '신용카드', '일반 신용카드 사용분'),
    ('DEBIT_CARD', '체크카드/현금영수증', '체크카드 및 현금영수증 사용분'),
    ('CASH_RECEIPT', '현금영수증', '현금영수증 사용분'),
    ('PUBLIC_TRANSPORT', '대중교통', '버스, 지하철, 철도 등 대중교통 사용분'),
    ('TRADITIONAL_MARKET', '전통시장', '전통시장 사용분'),
    ('CULTURE', '도서/공연/문화', '도서, 공연, 박물관, 미술관, 영화 등 문화비 사용분'),
    ('MEDICAL', '의료비', '의료비 세액공제 후보'),
    ('PENSION', '연금/IRP', '연금저축 및 IRP 납입 후보'),
    ('NON_DEDUCTIBLE', '공제 제외', '세금, 공과금, 상품권 등 공제 제외 후보')
ON CONFLICT (code) DO NOTHING;

INSERT INTO tax_rules (
    tax_year,
    category_id,
    deduction_rate,
    base_limit,
    additional_limit,
    min_income_threshold_rate,
    income_limit,
    source,
    effective_from
)
SELECT 2026, id, rate, base_limit, additional_limit, 0.2500, income_limit, 'MANUAL_SEED', DATE '2026-01-01'
FROM (
    VALUES
        ('CREDIT_CARD', 0.1500, 3000000, 0, NULL),
        ('DEBIT_CARD', 0.3000, 3000000, 0, NULL),
        ('CASH_RECEIPT', 0.3000, 3000000, 0, NULL),
        ('PUBLIC_TRANSPORT', 0.4000, 3000000, 3000000, NULL),
        ('TRADITIONAL_MARKET', 0.4000, 3000000, 3000000, NULL),
        ('CULTURE', 0.3000, 3000000, 3000000, 70000000),
        ('MEDICAL', 0.1500, 0, 0, NULL),
        ('PENSION', 0.1200, 0, 0, NULL),
        ('NON_DEDUCTIBLE', 0.0000, 0, 0, NULL)
) AS seed(code, rate, base_limit, additional_limit, income_limit)
JOIN tax_categories ON tax_categories.code = seed.code
ON CONFLICT (tax_year, category_id) DO NOTHING;
