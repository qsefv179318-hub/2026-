package com.livetaxlow.taxfeedback.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.ConfirmTossPaymentRequest;
import com.livetaxlow.taxfeedback.payment.TossDtos.TossPaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TossPaymentService {

    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentOrderService paymentOrderService;
    private final PaymentService paymentService;
    private final TossWebhookEventRepository tossWebhookEventRepository;

    public TossPaymentService(
            TossPaymentsClient tossPaymentsClient,
            PaymentOrderService paymentOrderService,
            PaymentService paymentService,
            TossWebhookEventRepository tossWebhookEventRepository
    ) {
        this.tossPaymentsClient = tossPaymentsClient;
        this.paymentOrderService = paymentOrderService;
        this.paymentService = paymentService;
        this.tossWebhookEventRepository = tossWebhookEventRepository;
    }

    @Transactional
    public Payment confirm(ConfirmTossPaymentRequest request) {
        paymentOrderService.getByOrderId(request.orderId());
        TossPaymentResponse response = tossPaymentsClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        return paymentService.saveConfirmedTossPayment(response);
    }

    @Transactional
    public void handleWebhook(JsonNode payload) {
        String eventId = text(payload, "eventId");
        String eventType = text(payload, "eventType");
        JsonNode data = payload.has("data") ? payload.get("data") : payload;
        String paymentKey = text(data, "paymentKey");
        String orderId = text(data, "orderId");
        String status = text(data, "status");

        if (eventId != null && tossWebhookEventRepository.findByEventId(eventId).isPresent()) {
            return;
        }

        TossWebhookEvent event = tossWebhookEventRepository.save(new TossWebhookEvent(
                eventId,
                eventType == null ? "UNKNOWN" : eventType,
                paymentKey,
                orderId,
                status,
                payload
        ));

        if (paymentKey == null || orderId == null) {
            throw new IllegalArgumentException("Toss webhook payload must contain paymentKey and orderId.");
        }

        paymentOrderService.getByOrderId(orderId);
        TossPaymentResponse tossPayment = tossPaymentsClient.getPayment(paymentKey);
        if (!orderId.equals(tossPayment.orderId())) {
            throw new IllegalArgumentException("Webhook orderId does not match Toss payment orderId.");
        }
        paymentService.saveConfirmedTossPayment(tossPayment);
        event.markProcessed();
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.hasNonNull(fieldName)) {
            return null;
        }
        return node.get(fieldName).asText();
    }
}
