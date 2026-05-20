package com.livetaxlow.taxfeedback.tax;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TaxRuleDtos {

    private TaxRuleDtos() {
    }

    public record TaxRuleResponse(
            Long id,
            int taxYear,
            String categoryCode,
            String categoryName,
            BigDecimal deductionRate,
            long baseLimit,
            long additionalLimit,
            BigDecimal minIncomeThresholdRate,
            Long incomeLimit,
            String source,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        static TaxRuleResponse from(TaxRule rule) {
            return new TaxRuleResponse(
                    rule.getId(),
                    rule.getTaxYear(),
                    rule.getCategory().getCode(),
                    rule.getCategory().getDisplayName(),
                    rule.getDeductionRate(),
                    rule.getBaseLimit(),
                    rule.getAdditionalLimit(),
                    rule.getMinIncomeThresholdRate(),
                    rule.getIncomeLimit(),
                    rule.getSource(),
                    rule.getEffectiveFrom(),
                    rule.getEffectiveTo()
            );
        }
    }

    public record SyncTaxRulesResponse(
            String provider,
            int taxYear,
            int upsertedCount,
            String status,
            String message
    ) {
    }
}
