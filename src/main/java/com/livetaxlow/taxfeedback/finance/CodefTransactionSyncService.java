package com.livetaxlow.taxfeedback.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodefTransactionSyncService {

    private static final String PROVIDER_CODEF = "CODEF";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final CodefClient codefClient;
    private final CodefProperties codefProperties;
    private final ExternalFinanceLinkRepository linkRepository;
    private final TransactionImportRunRepository importRunRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public CodefTransactionSyncService(
            CodefClient codefClient,
            CodefProperties codefProperties,
            ExternalFinanceLinkRepository linkRepository,
            TransactionImportRunRepository importRunRepository,
            UserTransactionRepository userTransactionRepository,
            ExpenseCategoryRepository expenseCategoryRepository,
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.codefClient = codefClient;
        this.codefProperties = codefProperties;
        this.linkRepository = linkRepository;
        this.importRunRepository = importRunRepository;
        this.userTransactionRepository = userTransactionRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ExternalFinanceLink registerLink(UUID userId, RegisterCodefLinkRequest request) {
        UserProfile user = userService.get(userId);
        Optional<ExternalFinanceLink> existing = linkRepository.findByUserIdAndProviderAndActiveTrue(userId, PROVIDER_CODEF);
        if (existing.isPresent()) {
            return existing.get();
        }

        JsonNode metadata = request.metadata() == null ? null : objectMapper.valueToTree(request.metadata());
        ExternalFinanceLink link = new ExternalFinanceLink(
                user,
                PROVIDER_CODEF,
                request.organization(),
                request.connectedId(),
                request.clientType() == null || request.clientType().isBlank() ? "PERSONAL" : request.clientType(),
                request.accountRef(),
                metadata
        );
        return linkRepository.save(link);
    }

    @Transactional
    public ExternalFinanceLink connectAndRegisterLink(UUID userId, ConnectCodefRequest request) {
        UserProfile user = userService.get(userId);
        Optional<ExternalFinanceLink> existing = linkRepository.findByUserIdAndProviderAndActiveTrue(userId, PROVIDER_CODEF);
        if (existing.isPresent()) {
            return existing.get();
        }

        JsonNode connectResponse = codefClient.createConnectedId(
                request.organization(),
                request.clientType(),
                request.accountRef(),
                request.credentials()
        );
        String connectedId = extractConnectedId(connectResponse);
        if (connectedId == null || connectedId.isBlank()) {
            throw new IllegalStateException("CODEF connectedId was not found in account create response.");
        }

        JsonNode metadata = objectMapper.valueToTree(Map.of(
                "connectResponse", connectResponse,
                "requestedAt", Instant.now().toString()
        ));
        ExternalFinanceLink link = new ExternalFinanceLink(
                user,
                PROVIDER_CODEF,
                request.organization(),
                connectedId,
                request.clientType() == null || request.clientType().isBlank() ? "PERSONAL" : request.clientType(),
                request.accountRef(),
                metadata
        );
        return linkRepository.save(link);
    }

    @Transactional
    public SyncResult syncTransactions(UUID userId, LocalDate fromDate, LocalDate toDate) {
        ExternalFinanceLink link = linkRepository.findByUserIdAndProviderAndActiveTrue(userId, PROVIDER_CODEF)
                .orElseThrow(() -> new NotFoundException("Active CODEF link not found for user: " + userId));

        TransactionImportRun run = importRunRepository.save(new TransactionImportRun(
                link.getUser(),
                PROVIDER_CODEF,
                fromDate,
                toDate
        ));

        try {
            Map<String, Object> query = new HashMap<>();
            query.put("startDate", fromDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            query.put("endDate", toDate.format(DateTimeFormatter.BASIC_ISO_DATE));

            JsonNode response = codefClient.fetchCardTransactions(link, query);
            List<JsonNode> txRows = extractList(response, codefProperties.responseListPath());
            for (JsonNode row : txRows) {
                ingestSingleTransaction(link.getUser(), row, run);
            }

            run.finishSuccess("CODEF sync completed.");
            return new SyncResult(run);
        } catch (Exception exception) {
            run.finishFailed(exception.getMessage());
            throw exception;
        }
    }

    private void ingestSingleTransaction(UserProfile user, JsonNode row, TransactionImportRun run) {
        CodefProperties.FieldMapping field = codefProperties.fieldMapping();

        String externalTxId = text(row, field.txId());
        if (externalTxId != null && userTransactionRepository.findByUserIdAndSourceAndExternalTxId(user.getId(), PROVIDER_CODEF, externalTxId).isPresent()) {
            run.addSkipped();
            return;
        }

        long amount = parseAmount(text(row, field.amount()));
        if (amount <= 0) {
            run.addFailed();
            return;
        }

        String merchantName = text(row, field.merchant());
        if (merchantName == null || merchantName.isBlank()) {
            merchantName = "UNKNOWN_MERCHANT";
        }
        String mcc = text(row, field.mcc());
        String methodText = text(row, field.paymentMethod());
        String categoryCode = classifyCategory(methodText, mcc, merchantName);
        ExpenseCategory category = expenseCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new NotFoundException("Expense category not found: " + categoryCode));

        Instant approvedAt = parseApprovedAt(text(row, field.txDate()), text(row, field.txTime()));
        String paymentMethod = normalizePaymentMethod(methodText);

        userTransactionRepository.save(new UserTransaction(
                user,
                category,
                merchantName,
                mcc,
                paymentMethod,
                amount,
                approvedAt,
                null,
                PROVIDER_CODEF,
                externalTxId
        ));
        run.addImported();
    }

    private List<JsonNode> extractList(JsonNode root, String path) {
        JsonNode cursor = root;
        for (String token : path.split("\\.")) {
            if (cursor == null || cursor.isMissingNode()) {
                return List.of();
            }
            cursor = cursor.path(token);
        }
        if (cursor == null || !cursor.isArray()) {
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        cursor.forEach(result::add);
        return result;
    }

    private String classifyCategory(String methodText, String mcc, String merchantName) {
        if (containsAny(merchantName, "시장")) {
            return "TRADITIONAL_MARKET";
        }
        if (containsAny(merchantName, "지하철", "버스", "교통")) {
            return "PUBLIC_TRANSPORT";
        }
        if (containsAny(merchantName, "병원", "의원", "약국")) {
            return "MEDICAL";
        }
        if (mcc != null && mcc.startsWith("41")) {
            return "PUBLIC_TRANSPORT";
        }
        if (mcc != null && mcc.startsWith("80")) {
            return "MEDICAL";
        }
        if (methodText != null && containsAny(methodText, "체크", "DEBIT", "직불")) {
            return "DEBIT_CARD";
        }
        if (methodText != null && containsAny(methodText, "현금")) {
            return "CASH_RECEIPT";
        }
        return "CREDIT_CARD";
    }

    private String normalizePaymentMethod(String methodText) {
        if (methodText == null) {
            return "UNKNOWN";
        }
        if (containsAny(methodText, "체크", "DEBIT", "직불")) {
            return "DEBIT";
        }
        if (containsAny(methodText, "현금")) {
            return "CASH";
        }
        if (containsAny(methodText, "계좌")) {
            return "BANK_TRANSFER";
        }
        return "CREDIT";
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Instant parseApprovedAt(String yyyymmdd, String hhmmss) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return Instant.now();
        }
        LocalDate date = LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime time;
        if (hhmmss != null && hhmmss.length() >= 6) {
            time = LocalTime.of(
                    Integer.parseInt(hhmmss.substring(0, 2)),
                    Integer.parseInt(hhmmss.substring(2, 4)),
                    Integer.parseInt(hhmmss.substring(4, 6))
            );
        } else {
            time = LocalTime.MIDNIGHT;
        }
        return LocalDateTime.of(date, time).atZone(KOREA_ZONE).toInstant();
    }

    private long parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        String numeric = value.replaceAll("[^0-9-]", "");
        if (numeric.isBlank()) {
            return 0;
        }
        return new BigDecimal(numeric).longValue();
    }

    private String text(JsonNode row, String key) {
        return key != null && row.hasNonNull(key) ? row.get(key).asText() : null;
    }

    private String extractConnectedId(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            return null;
        }
        if (root.hasNonNull("connectedId")) {
            return root.get("connectedId").asText();
        }
        JsonNode data = root.path("data");
        if (data.hasNonNull("connectedId")) {
            return data.get("connectedId").asText();
        }
        JsonNode result = root.path("result");
        if (result.hasNonNull("connectedId")) {
            return result.get("connectedId").asText();
        }
        JsonNode linked = root.path("data").path("connectedId");
        return linked.isMissingNode() || linked.isNull() ? null : linked.asText();
    }

    public record RegisterCodefLinkRequest(
            String organization,
            String connectedId,
            String clientType,
            String accountRef,
            Map<String, Object> metadata
    ) {
    }

    public record ConnectCodefRequest(
            String organization,
            String clientType,
            String accountRef,
            Map<String, Object> credentials
    ) {
    }

    public record SyncResult(
            String status,
            int importedCount,
            int skippedCount,
            int failedCount,
            String message
    ) {
        SyncResult(TransactionImportRun run) {
            this(
                    run.getStatus(),
                    run.getImportedCount(),
                    run.getSkippedCount(),
                    run.getFailedCount(),
                    run.getMessage()
            );
        }
    }
}
