package com.livetaxlow.taxfeedback.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public final class TossDtos {

    private TossDtos() {
    }

    public record TossConfirmRequest(
            String paymentKey,
            String orderId,
            long amount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TossPaymentResponse(
            String paymentKey,
            String orderId,
            String orderName,
            String status,
            String method,
            Long totalAmount,
            Long suppliedAmount,
            Long vat,
            Instant approvedAt,
            JsonNode easyPay,
            JsonNode card,
            JsonNode receipt
    ) {
        long resolvedAmount() {
            return totalAmount == null ? 0 : totalAmount;
        }
    }

    public record TossErrorResponse(
            String code,
            String message
    ) {
    }
}
