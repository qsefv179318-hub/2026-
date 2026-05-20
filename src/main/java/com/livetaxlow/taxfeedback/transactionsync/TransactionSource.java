package com.livetaxlow.taxfeedback.transactionsync;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Abstraction for transaction data providers (CODEF, mock file, etc.).
public interface TransactionSource {

    List<FetchedTransaction> fetchTransactions(
            UUID userId,
            String connectedId,
            String organization,
            LocalDate startDate,
            LocalDate endDate
    );

    record FetchedTransaction(
            String approvalNo,
            String merchantName,
            String storeType,
            long amount,
            String paymentMethod,
            Instant approvedAt
    ) {
    }
}

