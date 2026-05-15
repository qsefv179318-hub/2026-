-- 확장 모듈 설치 (UUID 생성을 위함)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. 사용자 테이블
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login_id VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    refresh_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. 연말정산 기본 레코드 (연도별/유형별 데이터)
CREATE TABLE tax_records (
    record_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    tax_year INTEGER NOT NULL,               -- 정산 대상 연도 (예: 2025)
    income_type VARCHAR(20) NOT NULL,        -- 'worker' (근로소득), 'business' (사업소득)
    annual_salary NUMERIC(15, 2) NOT NULL,   -- 총 연봉
    is_non_taxable_included BOOLEAN DEFAULT FALSE, -- 비과세 포함 여부
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. 정산 결과 테이블 (API 요약 정보와 연동)
CREATE TABLE tax_calculations (
    calculation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID REFERENCES tax_records(record_id) ON DELETE CASCADE UNIQUE,
    total_refund NUMERIC(15, 2) NOT NULL,    -- 기납부세액 - 결정세액
    refund_status_color VARCHAR(10) CHECK (refund_status_color IN ('RED', 'BLUE', 'WHITE')),
    determined_tax NUMERIC(15, 2) NOT NULL,  -- 실제 내야 할 세금 (결정세액)
    prepaid_tax NUMERIC(15, 2) NOT NULL,     -- 이미 낸 세금 (기납부세액)
    ai_summary_feedback TEXT                 -- 메인에 띄울 요약 피드백
);

-- 4. 소비 분석 통계 테이블
CREATE TABLE consumption_stats (
    stats_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID REFERENCES tax_records(record_id) ON DELETE CASCADE,
    total_spent NUMERIC(15, 2) NOT NULL,     -- 총 소비액
    credit_card_ratio NUMERIC(5, 2),          -- 신용카드 비중 (%)
    check_card_ratio NUMERIC(5, 2),           -- 체크카드/현금 비중 (%)
    category_data JSONB,                      -- 카테고리별 지출 (식비, 쇼핑 등 리스트)
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. AI 상세 피드백 테이블 (상세 페이지용)
CREATE TABLE ai_feedbacks (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID REFERENCES tax_records(record_id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT,                             -- AI 상세 가이드 본문
    saving_tip TEXT,                          -- 맞춤형 절세 팁
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. 업로드 파일 관리 테이블
CREATE TABLE uploaded_files (
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID REFERENCES tax_records(record_id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,                  -- 저장된 파일 경로 (S3 등)
    file_type VARCHAR(10),                    -- PDF, HTML 등
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);