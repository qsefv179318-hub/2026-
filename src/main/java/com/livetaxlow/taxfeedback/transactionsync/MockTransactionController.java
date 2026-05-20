package com.livetaxlow.taxfeedback.transactionsync;

import com.livetaxlow.taxfeedback.transactionsync.MockTransactionApiService.MockTransactionResponse;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/mock")
public class MockTransactionController {

    private final MockTransactionApiService mockTransactionApiService;

    public MockTransactionController(MockTransactionApiService mockTransactionApiService) {
        this.mockTransactionApiService = mockTransactionApiService;
    }

    @GetMapping("/transactions")
    List<MockTransactionResponse> getTransactions(@RequestParam @NotBlank String userId) {
        return mockTransactionApiService.getTransactions(userId);
    }
}

