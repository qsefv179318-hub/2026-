package com.livetaxlow.taxfeedback.calculation;

import java.util.List;

public final class RuleBasedDeductionDtos {

    private RuleBasedDeductionDtos() {
    }

    public record CategoryDeductionBreakdown(
            String categoryCode,
            long spending,
            long thresholdConsumed,
            long deductibleBase,
            long deductionBeforeLimit,
            long deductionAfterLimit
    ) {
    }

    public record RuleBasedDeductionResult(
            int taxYear,
            long annualIncome,
            long cardThresholdAmount,
            long medicalThresholdAmount,
            long medicalSpending,
            long cardThresholdRemaining,
            long cardTotalDeduction,
            long medicalDeductionBase,
            long estimatedTaxReduction,
            List<CategoryDeductionBreakdown> categories
    ) {
    }
}
