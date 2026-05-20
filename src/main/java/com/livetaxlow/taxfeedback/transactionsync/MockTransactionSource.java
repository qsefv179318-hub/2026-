package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Transaction source backed by external JSON file (not hard-coded in Java code).
public class MockTransactionSource implements TransactionSource {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;
    private final SyncSourceProperties properties;

    public MockTransactionSource(ObjectMapper objectMapper, SyncSourceProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public List<FetchedTransaction> fetchTransactions(
            UUID userId,
            String connectedId,
            String organization,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String file = properties.mockFile() == null || properties.mockFile().isBlank()
                ? "mock/mock-data.json"
                : properties.mockFile();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(file)) {
            if (input == null) {
                return List.of();
            }
            List<MockRow> rows = objectMapper.readValue(input, new TypeReference<List<MockRow>>() {
            });
            List<FetchedTransaction> result = new ArrayList<>();
            for (MockRow row : rows) {
                LocalDate txDate = LocalDate.parse(row.approvedAt().substring(0, 10));
                if (txDate.isBefore(startDate) || txDate.isAfter(endDate)) {
                    continue;
                }
                result.add(new FetchedTransaction(
                        row.approvalNo(),
                        row.merchantName(),
                        row.storeType(),
                        row.amount(),
                        row.paymentMethod(),
                        LocalDateTime.parse(row.approvedAt(), TS).atZone(KOREA_ZONE).toInstant()
                ));
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load mock transaction file.", exception);
        }
    }

    record MockRow(
            String approvalNo,
            String merchantName,
            String storeType,
            long amount,
            String paymentMethod,
            String approvedAt
    ) {
    }
}
