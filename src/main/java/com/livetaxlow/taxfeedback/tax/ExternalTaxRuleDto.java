package com.livetaxlow.taxfeedback.tax;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExternalTaxRuleDto(
        int taxYear,
        String categoryCode,
        BigDecimal deductionRate,
        long baseLimit,
        long additionalLimit,
        BigDecimal minIncomeThresholdRate,
        Long incomeLimit,
        String source,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
