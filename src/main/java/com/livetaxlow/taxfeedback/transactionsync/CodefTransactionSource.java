package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.databind.JsonNode;
import com.livetaxlow.taxfeedback.finance.CodefClient;
import com.livetaxlow.taxfeedback.finance.CodefProperties;
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
import java.util.UUID;

// Transaction source backed by CODEF approval-list API.
public class CodefTransactionSource implements TransactionSource {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final CodefClient codefClient;
    private final CodefProperties codefProperties;

    public CodefTransactionSource(CodefClient codefClient, CodefProperties codefProperties) {
        this.codefClient = codefClient;
        this.codefProperties = codefProperties;
    }

    @Override
    public List<FetchedTransaction> fetchTransactions(
            UUID userId,
            String connectedId,
            String organization,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<String, Object> query = new HashMap<>();
        query.put("startDate", startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        query.put("endDate", endDate.format(DateTimeFormatter.BASIC_ISO_DATE));

        JsonNode response = codefClient.fetchCardTransactions(organization, connectedId, "PERSONAL", null, query);
        List<JsonNode> rows = extractRows(response);

        List<FetchedTransaction> result = new ArrayList<>();
        for (JsonNode row : rows) {
            String approvalNo = text(row, codefProperties.fieldMapping().txId());
            if (approvalNo == null || approvalNo.isBlank()) {
                continue;
            }
            String merchantName = text(row, "resMemberStoreName");
            String storeType = text(row, "resMemberStoreType");
            long amount = parseAmount(text(row, "resUsedAmount"));
            Instant approvedAt = parseApprovedAt(
                    text(row, codefProperties.fieldMapping().txDate()),
                    text(row, codefProperties.fieldMapping().txTime())
            );

            result.add(new FetchedTransaction(
                    approvalNo,
                    merchantName == null || merchantName.isBlank() ? "UNKNOWN_MERCHANT" : merchantName,
                    storeType,
                    amount,
                    normalizePaymentMethod(text(row, codefProperties.fieldMapping().paymentMethod())),
                    approvedAt
            ));
        }
        return result;
    }

    private List<JsonNode> extractRows(JsonNode root) {
        JsonNode cursor = root;
        for (String token : codefProperties.responseListPath().split("\\.")) {
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

    private String text(JsonNode row, String key) {
        return key != null && row.hasNonNull(key) ? row.get(key).asText() : null;
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

    private Instant parseApprovedAt(String yyyymmdd, String hhmmss) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return Instant.now();
        }
        LocalDate date = LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime time = LocalTime.MIDNIGHT;
        if (hhmmss != null && hhmmss.length() >= 6) {
            time = LocalTime.of(
                    Integer.parseInt(hhmmss.substring(0, 2)),
                    Integer.parseInt(hhmmss.substring(2, 4)),
                    Integer.parseInt(hhmmss.substring(4, 6))
            );
        }
        return LocalDateTime.of(date, time).atZone(KOREA_ZONE).toInstant();
    }

    private String normalizePaymentMethod(String value) {
        if (value == null) {
            return "UNKNOWN";
        }
        if (value.contains("체크") || value.toUpperCase().contains("DEBIT")) {
            return "DEBIT";
        }
        if (value.contains("현금")) {
            return "CASH";
        }
        return "CREDIT";
    }
}

