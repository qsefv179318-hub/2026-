package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.payment.PaymentDtos.CreatePaymentOrderRequest;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.PaymentOrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-orders")
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;

    public PaymentOrderController(PaymentOrderService paymentOrderService) {
        this.paymentOrderService = paymentOrderService;
    }

    @PostMapping
    PaymentOrderResponse create(@Valid @RequestBody CreatePaymentOrderRequest request) {
        return PaymentOrderResponse.from(paymentOrderService.create(request));
    }

    @GetMapping("/{orderId}")
    PaymentOrderResponse get(@PathVariable String orderId) {
        return PaymentOrderResponse.from(paymentOrderService.getByOrderId(orderId));
    }
}
