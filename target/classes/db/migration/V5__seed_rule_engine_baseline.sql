INSERT INTO expense_categories (code, name, description)
VALUES
    ('CREDIT_CARD', '신용카드', '신용카드 사용액'),
    ('DEBIT_CARD', '체크카드', '체크카드 사용액'),
    ('TRADITIONAL_MARKET', '전통시장', '전통시장 사용액'),
    ('PUBLIC_TRANSPORT', '대중교통', '대중교통 사용액'),
    ('MEDICAL', '의료비', '의료비 지출'),
    ('INSURANCE', '보험료', '보험료 지출'),
    ('MONTHLY_RENT', '월세', '월세 지출')
ON CONFLICT (code) DO NOTHING;

INSERT INTO tax_computation_parameters (tax_year, parameter_key, parameter_value, description)
VALUES
    (2026, 'CARD_DEDUCTION_THRESHOLD_RATE', 0.25, '총급여 대비 카드 공제 문턱 비율'),
    (2026, 'MEDICAL_DEDUCTION_THRESHOLD_RATE', 0.03, '총급여 대비 의료비 세액공제 문턱 비율'),
    (2026, 'CARD_DEDUCTION_TOTAL_LIMIT', 3000000, '카드 소득공제 총 한도'),
    (2026, 'ESTIMATED_MARGINAL_TAX_RATE', 0.15, '예상 세부담률(단순 추정용)')
ON CONFLICT (tax_year, parameter_key) DO NOTHING;

INSERT INTO deduction_category_rules (
    tax_year,
    category_code,
    deduction_rate,
    annual_limit,
    threshold_fill_priority,
    applies_to_card_threshold
)
VALUES
    (2026, 'CREDIT_CARD', 0.150000, 3000000, 1, true),
    (2026, 'DEBIT_CARD', 0.300000, 3000000, 2, true),
    (2026, 'TRADITIONAL_MARKET', 0.400000, 3000000, 3, true),
    (2026, 'PUBLIC_TRANSPORT', 0.400000, 3000000, 4, true),
    (2026, 'MEDICAL', 0.150000, NULL, 99, false),
    (2026, 'INSURANCE', 0.120000, NULL, 99, false),
    (2026, 'MONTHLY_RENT', 0.100000, NULL, 99, false)
ON CONFLICT (tax_year, category_code) DO NOTHING;
