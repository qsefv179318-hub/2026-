package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.payment.PaymentDtos.CreatePaymentRequest;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.PaymentResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    PaymentResponse create(@PathVariable UUID userId, @Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(paymentService.create(userId, request));
    }

    @GetMapping
    List<PaymentResponse> list(@PathVariable UUID userId) {
        return paymentService.list(userId).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
