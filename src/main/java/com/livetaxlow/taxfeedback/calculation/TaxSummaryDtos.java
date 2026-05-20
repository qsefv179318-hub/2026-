package com.livetaxlow.taxfeedback.calculation;

import java.util.List;

public final class TaxSummaryDtos {

    private TaxSummaryDtos() {
    }

    public record CategorySummary(
            String categoryCode,
            String categoryName,
            long spendingAmount,
            long deductibleBaseAmount,
            long incomeDeductionAmount,
            long estimatedTaxBenefit
    ) {
    }

    public record TaxSummaryResponse(
            int taxYear,
            long annualIncome,
            long minimumSpendThreshold,
            long totalSpendingAmount,
            long deductibleSpendingAmount,
            long incomeDeductionAmount,
            long estimatedTaxBenefit,
            List<CategorySummary> categories
    ) {
    }
}
