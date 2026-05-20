package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockTransactionApiService {

    private static final String MOCK_FILE = "mock/mock-data.json";

    private final ObjectMapper objectMapper;

    public MockTransactionApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<MockTransactionResponse> getTransactions(String userId) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(MOCK_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Mock transaction file not found: " + MOCK_FILE);
            }
            return objectMapper.readValue(input, new TypeReference<List<MockTransactionResponse>>() {
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read mock transaction file.", exception);
        }
    }

    public record MockTransactionResponse(
            String approvalNo,
            String merchantName,
            String storeType,
            long amount,
            String paymentMethod,
            String approvedAt
    ) {
    }
}

