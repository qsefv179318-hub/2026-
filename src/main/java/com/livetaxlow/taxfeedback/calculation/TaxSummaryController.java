package com.livetaxlow.taxfeedback.calculation;

import com.livetaxlow.taxfeedback.calculation.TaxSummaryDtos.TaxSummaryResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tax-summary")
public class TaxSummaryController {

    private final TaxCalculationService taxCalculationService;

    public TaxSummaryController(TaxCalculationService taxCalculationService) {
        this.taxCalculationService = taxCalculationService;
    }

    @GetMapping
    TaxSummaryResponse summarize(
            @PathVariable UUID userId,
            @RequestParam(required = false) Integer taxYear
    ) {
        int targetYear = taxYear == null ? LocalDate.now().getYear() : taxYear;
        return taxCalculationService.summarize(userId, targetYear);
    }
}
