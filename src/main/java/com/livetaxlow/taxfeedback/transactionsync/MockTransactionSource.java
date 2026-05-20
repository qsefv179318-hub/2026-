package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
            long batchSeed = System.currentTimeMillis();
            int index = 0;

            for (MockRow row : rows) {
                LocalDate txDate = LocalDate.parse(row.approvedAt().substring(0, 10));
                if (txDate.isBefore(startDate) || txDate.isAfter(endDate)) {
                    continue;
                }

                LocalDateTime baseTime = LocalDateTime.parse(row.approvedAt(), TS);
                int minuteShift = ThreadLocalRandom.current().nextInt(-180, 181);
                LocalDateTime randomizedTime = baseTime.plusMinutes(minuteShift);

                // Keep each sync run unique so frontend can see changed numbers on every new visit.
                String randomizedApprovalNo = row.approvalNo() + "-" + batchSeed + "-" + (index++);
                long randomizedAmount = randomizeAmount(row.amount());
                String randomizedPaymentMethod = randomizePaymentMethod(row.paymentMethod());

                result.add(new FetchedTransaction(
                        randomizedApprovalNo,
                        row.merchantName(),
                        row.storeType(),
                        randomizedAmount,
                        randomizedPaymentMethod,
                        randomizedTime.atZone(KOREA_ZONE).toInstant()
                ));
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load mock transaction file.", exception);
        }
    }

    private long randomizeAmount(long baseAmount) {
        long min = Math.max(1000L, (long) (baseAmount * 0.7));
        long max = Math.max(min + 1000L, (long) (baseAmount * 2.1));
        long sampled = ThreadLocalRandom.current().nextLong(min, max + 1);
        return (sampled / 100) * 100;
    }

    private String randomizePaymentMethod(String original) {
        if (original == null || original.isBlank()) {
            return ThreadLocalRandom.current().nextBoolean() ? "CREDIT" : "DEBIT";
        }
        String[] methods = {"CREDIT", "DEBIT", "CASH"};
        if (ThreadLocalRandom.current().nextInt(10) < 7) {
            return original.toUpperCase();
        }
        return methods[ThreadLocalRandom.current().nextInt(methods.length)];
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
