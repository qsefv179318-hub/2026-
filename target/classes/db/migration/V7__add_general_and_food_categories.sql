INSERT INTO expense_categories (code, name, description)
VALUES
    ('FOOD', '식비', '식당, 카페, 음식 관련 지출'),
    ('GENERAL', '일반소비', '기타 일반 소비 지출')
ON CONFLICT (code) DO NOTHING;
