package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.payment.PaymentDtos.ConfirmTossPaymentRequest;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/toss")
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    public TossPaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    @PostMapping("/confirm")
    PaymentResponse confirm(@Valid @RequestBody ConfirmTossPaymentRequest request) {
        return PaymentResponse.from(tossPaymentService.confirm(request));
    }
}
