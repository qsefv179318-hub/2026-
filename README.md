# LiveTaxLow Backend

연말정산 실시간 피드백 서비스를 위한 Spring Boot 백엔드 뼈대입니다.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA, Validation
- PostgreSQL
- Flyway
- springdoc-openapi

## Local DB

```bash
cd backend
docker compose up -d
```

기본 접속 정보:

- DB: `livetaxlow`
- User: `livetaxlow`
- Password: `livetaxlow`
- Port: `15432`

## Docker Handoff (Windows/Linux/macOS)

프론트엔드 개발자가 로컬에서 바로 백엔드를 띄우는 표준 방식입니다.

1) 프로젝트 클론 후 `backend` 폴더로 이동  
2) `.env.example`을 `.env`로 복사  
3) 필요 시 `CODEF_*`, `OPENAI_API_KEY`, `SYNC_SOURCE_TYPE` 수정  
4) Docker로 앱+DB 동시 실행

```bash
cd backend
cp .env.example .env
docker compose up --build -d
```

Windows PowerShell 예시:

```powershell
cd ".\backend"
copy .env.example .env
docker compose up --build -d
```

확인 주소:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/docs`
- PostgreSQL: `localhost:15432`

중지:

```bash
docker compose down
```

## Run

Maven 설치 후 실행합니다.

```bash
cd backend
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/docs
```

## Core Tables

- `users`: 예상 연봉, 부양가족 수
- `tax_categories`: 신용카드, 체크카드, 대중교통, 전통시장 등 공제 분류
- `tax_rules`: 과세연도별 공제율, 한도, 소득 기준
- `payments`: 토스페이먼츠 웹훅 또는 수동 등록 결제 내역
- `feedbacks`: 결제별 실시간 피드백
- `tax_rule_sync_logs`: 외부 세법 규칙 동기화 로그
- `withholding_tax_tables`, `withholding_tax_brackets`, `withholding_tax_amounts`: 근로소득 간이세액표(Grid) 정규화 저장
- `expense_categories`, `user_transactions`: 사용자 지출 카테고리/거래 저장
- `tax_computation_parameters`, `deduction_category_rules`: 규칙 기반 계산 엔진 파라미터/카테고리 룰

## Seed Rules

`V2__seed_2026_tax_rules.sql`에 MVP용 2026년 규칙을 넣어두었습니다.
실서비스 전에는 국세청/공공데이터/관리자 검수 데이터로 교체해야 합니다.
`V5__seed_rule_engine_baseline.sql`에는 규칙 엔진의 기본 파라미터(25%, 3%, 카드총한도)와 카테고리별 공제 룰을 넣어두었습니다.

## API Flow

### CODEF 기반 거래 수집 플로우 (권장)

1) 사용자 생성  
2) CODEF 연동 링크 등록 (`connectedId`, `organization`) 또는 버튼 기반 직접 동기화  
3) 기간 기준 거래 동기화 (버튼 API는 최근 30일 고정)  
4) 규칙 엔진 미리보기로 공제 계산 확인

### 1. Create User

```bash
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"annualIncome":42000000,"dependentsCount":0}'
```

### 1-1. Register CODEF Link

```bash
curl -X POST http://localhost:8080/api/codef/users/{userId}/links \
  -H 'Content-Type: application/json' \
  -d '{
    "organization":"0004",
    "connectedId":"your-connected-id",
    "clientType":"PERSONAL"
  }'
```

### 1-2. Sync CODEF Transactions

```bash
curl -X POST "http://localhost:8080/api/codef/users/{userId}/sync-transactions?fromDate=2026-01-01&toDate=2026-12-31"
```

### 1-3. Button Trigger Sync (최근 30일 고정)

`POST /api/transactions/sync`는 요청 시점 기준 최근 30일(`오늘-29일 ~ 오늘`)을 자동 계산하여 CODEF API를 호출합니다.
요청에 필요한 핵심 파라미터는 `connectedId`, `organization`입니다.

```bash
curl -X POST "http://localhost:8080/api/transactions/sync" \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"{userId}",
    "connectedId":"your-connected-id",
    "organization":"0004"
  }'
```

### 2. Create Payment Order

프론트엔드는 Toss 결제창을 띄우기 전에 우리 서버에서 주문을 먼저 만듭니다.
응답의 `orderId`, `orderName`, `amount`를 Toss 결제창 요청에 사용합니다.

```bash
curl -X POST http://localhost:8080/api/payment-orders \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"{userId}",
    "orderName":"스타벅스 결제",
    "amount":6800,
    "merchantName":"스타벅스",
    "merchantCategoryCode":"5814"
  }'
```

### 3. Confirm Toss Payment

Toss 결제 성공 페이지에서 받은 `paymentKey`, `orderId`, `amount`를 백엔드로 보냅니다.
백엔드는 `TOSS_SECRET_KEY`로 Toss 승인 API를 호출하고, 성공하면 `payments`에 저장한 뒤 피드백을 생성합니다.

```bash
export TOSS_SECRET_KEY='test_sk_...'

curl -X POST http://localhost:8080/api/payments/toss/confirm \
  -H 'Content-Type: application/json' \
  -d '{
    "paymentKey":"{paymentKey}",
    "orderId":"{orderId}",
    "amount":6800
  }'
```

### 4. Toss Webhook

Toss 개발자센터의 웹훅 URL은 아래로 설정합니다.

```text
POST http://{public-domain}/api/payments/webhook/toss
```

로컬 테스트에서는 ngrok 같은 터널을 사용해 `localhost:8080`을 외부에 노출해야 합니다.
웹훅이 들어오면 서버는 `paymentKey`로 Toss 결제 조회 API를 호출해 상태를 검증하고 저장합니다.

### 5. Create Payment Manually

```bash
curl -X POST http://localhost:8080/api/users/{userId}/payments \
  -H 'Content-Type: application/json' \
  -d '{
    "tossOrderId":"order-001",
    "paymentKey":"payment-key-001",
    "merchantName":"스타벅스",
    "merchantCategoryCode":"5814",
    "amount":6800,
    "method":"CREDIT",
    "approvedAt":"2026-05-20T03:00:00Z"
  }'
```

### 6. Get Realtime Feedback

```bash
curl http://localhost:8080/api/users/{userId}/feedbacks
```

### 7. Get Tax Summary

```bash
curl 'http://localhost:8080/api/users/{userId}/tax-summary?taxYear=2026'
```

### 8. Sync Tax Rules From External API

환경변수 `TAX_RULES_EXTERNAL_BASE_URL`이 설정되면 다음 형식의 API를 호출합니다.

```text
GET {TAX_RULES_EXTERNAL_BASE_URL}/tax-rules?taxYear=2026
```

응답 예시:

```json
[
  {
    "taxYear": 2026,
    "categoryCode": "CREDIT_CARD",
    "deductionRate": 0.15,
    "baseLimit": 3000000,
    "additionalLimit": 0,
    "minIncomeThresholdRate": 0.25,
    "incomeLimit": null,
    "source": "NTS",
    "effectiveFrom": "2026-01-01",
    "effectiveTo": null
  }
]
```

동기화 실행:

```bash
curl -X POST 'http://localhost:8080/api/tax-rules/sync?taxYear=2026'
```

### 9. Rule Engine Preview (실결제 기반)

```bash
curl 'http://localhost:8080/api/deduction-engine/users/{userId}/preview?taxYear=2026'
```

### 10. Rule Engine Simulation (입력값 기반)

```bash
curl -X POST 'http://localhost:8080/api/deduction-engine/simulate' \
  -H 'Content-Type: application/json' \
  -d '{
    "annualIncome":42000000,
    "taxYear":2026,
    "spendingByCategory":{
      "CREDIT_CARD":12000000,
      "DEBIT_CARD":6000000,
      "TRADITIONAL_MARKET":1000000,
      "PUBLIC_TRANSPORT":800000,
      "MEDICAL":2500000
    }
  }'
```

## Notes

- 현재 계산 로직은 MVP용입니다. 총급여 25% 초과분, 카테고리별 공제율, 기본 한도 적용 흐름을 먼저 구현했습니다.
- 실제 환급금은 결정세액, 기납부세액, 세액공제, 인적공제, 특별공제 등을 함께 반영해야 합니다.
- Toss 연동은 `TOSS_SECRET_KEY`만 넣으면 승인 API와 결제 조회 API를 호출하도록 구현되어 있습니다.
- AI 피드백은 `OPENAI_API_KEY`가 없으면 로컬 규칙 기반 문장으로 동작하고, 키가 있으면 OpenAI Responses API로 짧은 한국어 피드백을 생성합니다.
- CODEF 연동은 `CODEF_ENABLED=true`, `CODEF_CLIENT_ID`, `CODEF_CLIENT_SECRET`가 필수이며, 거래 응답 필드명이 다르면 `codef.field-mapping.*` 환경변수로 맞춥니다.
