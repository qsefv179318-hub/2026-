package com.livetaxlow.taxfeedback.payment;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhook/toss")
public class TossWebhookController {

    private final TossPaymentService tossPaymentService;

    public TossWebhookController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    @PostMapping
    Map<String, String> receive(@RequestBody JsonNode payload) {
        tossPaymentService.handleWebhook(payload);
        return Map.of("status", "ok");
    }
}
