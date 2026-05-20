package com.livetaxlow.taxfeedback.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreatePaymentRequest(
            String tossOrderId,
            String paymentKey,
            @NotBlank String merchantName,
            String merchantCategoryCode,
            @NotNull @Min(1) Long amount,
            @NotNull PaymentMethod method,
            Instant approvedAt
    ) {
    }

    public record CreatePaymentOrderRequest(
            @NotNull UUID userId,
            @NotBlank String orderName,
            @NotNull @Min(1) Long amount,
            @NotBlank String merchantName,
            String merchantCategoryCode
    ) {
    }

    public record PaymentOrderResponse(
            UUID id,
            UUID userId,
            String orderId,
            String orderName,
            long amount,
            String merchantName,
            String merchantCategoryCode,
            PaymentStatus status,
            Instant createdAt
    ) {
        static PaymentOrderResponse from(PaymentOrder order) {
            return new PaymentOrderResponse(
                    order.getId(),
                    order.getUser().getId(),
                    order.getOrderId(),
                    order.getOrderName(),
                    order.getAmount(),
                    order.getMerchantName(),
                    order.getMerchantCategoryCode(),
                    order.getStatus(),
                    order.getCreatedAt()
            );
        }
    }

    public record ConfirmTossPaymentRequest(
            @NotBlank String paymentKey,
            @NotBlank String orderId,
            @NotNull @Min(1) Long amount
    ) {
    }

    public record TossWebhookRequest(
            @NotNull UUID userId,
            String orderId,
            String paymentKey,
            @NotBlank String merchantName,
            String mcc,
            @NotNull @Min(1) Long amount,
            @NotNull PaymentMethod method,
            Instant approvedAt
    ) {
    }

    public record PaymentResponse(
            UUID id,
            String tossOrderId,
            String merchantName,
            String merchantCategoryCode,
            long amount,
            PaymentMethod method,
            PaymentStatus status,
            String categoryCode,
            String categoryName,
            boolean deductible,
            Instant approvedAt
    ) {
        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getTossOrderId(),
                    payment.getMerchantName(),
                    payment.getMerchantCategoryCode(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getStatus(),
                    payment.getCategory().getCode(),
                    payment.getCategory().getDisplayName(),
                    payment.isDeductible(),
                    payment.getApprovedAt()
            );
        }
    }
}
