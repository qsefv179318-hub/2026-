package com.livetaxlow.taxfeedback.calculation;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.RuleBasedDeductionResult;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@CrossOrigin(origins = "*")
@Validated
@RestController
@RequestMapping("/api/deduction-engine")
public class RuleBasedDeductionController {

    private final RuleBasedDeductionEngineService engineService;

    public RuleBasedDeductionController(RuleBasedDeductionEngineService engineService) {
        this.engineService = engineService;
    }

    @GetMapping("/users/{userId}/preview")
    RuleBasedDeductionResult preview(
            @PathVariable UUID userId,
            @RequestParam(required = false) Integer taxYear
    ) {
        int targetYear = taxYear == null ? LocalDate.now().getYear() : taxYear;
        return engineService.preview(userId, targetYear);
    }

    @PostMapping("/simulate")
    RuleBasedDeductionResult simulate(@RequestBody DeductionSimulationRequest request) {
        return engineService.calculate(request.annualIncome(), request.taxYear(), request.spendingByCategory());
    }

    public record DeductionSimulationRequest(
            @NotNull @Min(1) Long annualIncome,
            @NotNull Integer taxYear,
            @NotNull Map<String, Long> spendingByCategory
    ) {
    }
}
