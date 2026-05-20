package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.feedback.FeedbackService;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.CreatePaymentRequest;
import com.livetaxlow.taxfeedback.payment.TossDtos.TossPaymentResponse;
import com.livetaxlow.taxfeedback.tax.TaxCategory;
import com.livetaxlow.taxfeedback.tax.TaxCategoryRepository;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TaxCategoryRepository taxCategoryRepository;
    private final UserService userService;
    private final PaymentCategoryClassifier classifier;
    private final FeedbackService feedbackService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            TaxCategoryRepository taxCategoryRepository,
            UserService userService,
            PaymentCategoryClassifier classifier,
            FeedbackService feedbackService,
            PaymentOrderRepository paymentOrderRepository,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.taxCategoryRepository = taxCategoryRepository;
        this.userService = userService;
        this.classifier = classifier;
        this.feedbackService = feedbackService;
        this.paymentOrderRepository = paymentOrderRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment create(UUID userId, CreatePaymentRequest request) {
        if (request.tossOrderId() != null) {
            Payment existing = paymentRepository.findByTossOrderId(request.tossOrderId()).orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        UserProfile user = userService.get(userId);
        String categoryCode = classifier.classify(request.method(), request.merchantCategoryCode());
        TaxCategory category = taxCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new NotFoundException("Tax category not found: " + categoryCode));
        boolean deductible = !"NON_DEDUCTIBLE".equals(categoryCode);

        Payment payment = new Payment(
                user,
                request.tossOrderId(),
                request.paymentKey(),
                request.merchantName(),
                request.merchantCategoryCode(),
                request.amount(),
                request.method(),
                PaymentStatus.DONE,
                category,
                deductible,
                request.approvedAt() == null ? Instant.now() : request.approvedAt(),
                null
        );

        Payment savedPayment = paymentRepository.save(payment);
        feedbackService.generateForPayment(user, savedPayment);
        return savedPayment;
    }

    @Transactional
    public Payment saveConfirmedTossPayment(TossPaymentResponse tossPayment) {
        Payment existingByPaymentKey = paymentRepository.findByPaymentKey(tossPayment.paymentKey()).orElse(null);
        if (existingByPaymentKey != null) {
            existingByPaymentKey.updateStatus(parseStatus(tossPayment.status()));
            return existingByPaymentKey;
        }

        PaymentOrder order = paymentOrderRepository.findByOrderId(tossPayment.orderId())
                .orElseThrow(() -> new NotFoundException("Payment order not found: " + tossPayment.orderId()));

        if (order.getAmount() != tossPayment.resolvedAmount()) {
            throw new IllegalArgumentException("Toss payment amount does not match order amount.");
        }

        String categoryCode = classifier.classify(parseMethod(tossPayment.method()), order.getMerchantCategoryCode());
        TaxCategory category = taxCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new NotFoundException("Tax category not found: " + categoryCode));
        boolean deductible = !"NON_DEDUCTIBLE".equals(categoryCode);
        PaymentStatus status = parseStatus(tossPayment.status());
        order.updateStatus(status);

        Payment payment = new Payment(
                order.getUser(),
                tossPayment.orderId(),
                tossPayment.paymentKey(),
                order.getMerchantName(),
                order.getMerchantCategoryCode(),
                tossPayment.resolvedAmount(),
                parseMethod(tossPayment.method()),
                status,
                category,
                deductible,
                tossPayment.approvedAt() == null ? Instant.now() : tossPayment.approvedAt(),
                toJson(tossPayment)
        );

        Payment savedPayment = paymentRepository.save(payment);
        if (status == PaymentStatus.DONE) {
            feedbackService.generateForPayment(order.getUser(), savedPayment);
        }
        return savedPayment;
    }

    @Transactional(readOnly = true)
    public List<Payment> list(UUID userId) {
        return paymentRepository.findByUserIdOrderByApprovedAtDesc(userId);
    }

    private PaymentMethod parseMethod(String tossMethod) {
        if (tossMethod == null) {
            return PaymentMethod.OTHER;
        }
        if (tossMethod.contains("카드")) {
            return PaymentMethod.CREDIT;
        }
        if (tossMethod.contains("계좌")) {
            return PaymentMethod.BANK_TRANSFER;
        }
        if (tossMethod.contains("현금")) {
            return PaymentMethod.CASH;
        }
        return PaymentMethod.OTHER;
    }

    private PaymentStatus parseStatus(String tossStatus) {
        if (tossStatus == null) {
            return PaymentStatus.UNKNOWN;
        }
        try {
            return PaymentStatus.valueOf(tossStatus);
        } catch (IllegalArgumentException ignored) {
            return PaymentStatus.UNKNOWN;
        }
    }

    private JsonNode toJson(TossPaymentResponse tossPayment) {
        return objectMapper.valueToTree(tossPayment);
    }
}
