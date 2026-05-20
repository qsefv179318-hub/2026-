package com.livetaxlow.taxfeedback.calculation;

import com.livetaxlow.taxfeedback.calculation.TaxSummaryDtos.CategorySummary;
import com.livetaxlow.taxfeedback.calculation.TaxSummaryDtos.TaxSummaryResponse;
import com.livetaxlow.taxfeedback.payment.Payment;
import com.livetaxlow.taxfeedback.payment.PaymentRepository;
import com.livetaxlow.taxfeedback.tax.TaxRule;
import com.livetaxlow.taxfeedback.tax.TaxRuleRepository;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxCalculationService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final TaxRuleRepository taxRuleRepository;

    public TaxCalculationService(
            UserService userService,
            PaymentRepository paymentRepository,
            TaxRuleRepository taxRuleRepository
    ) {
        this.userService = userService;
        this.paymentRepository = paymentRepository;
        this.taxRuleRepository = taxRuleRepository;
    }

    @Transactional(readOnly = true)
    public TaxSummaryResponse summarize(UUID userId, int taxYear) {
        UserProfile user = userService.get(userId);
        List<Payment> payments = paymentRepository.findByUserIdAndApprovedAtBetweenOrderByApprovedAtDesc(
                userId,
                yearStart(taxYear),
                yearStart(taxYear + 1)
        );
        Map<String, TaxRule> rulesByCategory = taxRuleRepository.findByTaxYear(taxYear).stream()
                .collect(Collectors.toMap(rule -> rule.getCategory().getCode(), Function.identity()));

        long threshold = BigDecimal.valueOf(user.getAnnualIncome())
                .multiply(BigDecimal.valueOf(0.25))
                .setScale(0, RoundingMode.DOWN)
                .longValue();
        long totalSpending = payments.stream().mapToLong(Payment::getAmount).sum();
        long deductibleSpending = payments.stream()
                .filter(Payment::isDeductible)
                .mapToLong(Payment::getAmount)
                .sum();
        long overThresholdAmount = Math.max(0, deductibleSpending - threshold);

        List<CategorySummary> categorySummaries = payments.stream()
                .filter(Payment::isDeductible)
                .collect(Collectors.groupingBy(payment -> payment.getCategory().getCode()))
                .entrySet()
                .stream()
                .map(entry -> summarizeCategory(entry.getKey(), entry.getValue(), rulesByCategory, overThresholdAmount, deductibleSpending))
                .sorted(Comparator.comparing(CategorySummary::incomeDeductionAmount).reversed())
                .toList();

        long incomeDeductionAmount = categorySummaries.stream()
                .mapToLong(CategorySummary::incomeDeductionAmount)
                .sum();
        long estimatedTaxBenefit = estimateTaxBenefit(user.getAnnualIncome(), incomeDeductionAmount);

        return new TaxSummaryResponse(
                taxYear,
                user.getAnnualIncome(),
                threshold,
                totalSpending,
                deductibleSpending,
                incomeDeductionAmount,
                estimatedTaxBenefit,
                categorySummaries
        );
    }

    private CategorySummary summarizeCategory(
            String categoryCode,
            List<Payment> payments,
            Map<String, TaxRule> rulesByCategory,
            long overThresholdAmount,
            long deductibleSpending
    ) {
        TaxRule rule = rulesByCategory.get(categoryCode);
        long spendingAmount = payments.stream().mapToLong(Payment::getAmount).sum();
        long deductibleBaseAmount = deductibleSpending == 0
                ? 0
                : BigDecimal.valueOf(overThresholdAmount)
                .multiply(BigDecimal.valueOf(spendingAmount))
                .divide(BigDecimal.valueOf(deductibleSpending), 0, RoundingMode.DOWN)
                .longValue();
        long incomeDeductionAmount = rule == null
                ? 0
                : BigDecimal.valueOf(deductibleBaseAmount)
                .multiply(rule.getDeductionRate())
                .setScale(0, RoundingMode.DOWN)
                .longValue();
        long limitedDeductionAmount = rule == null || rule.getBaseLimit() == 0
                ? incomeDeductionAmount
                : Math.min(incomeDeductionAmount, rule.getBaseLimit() + rule.getAdditionalLimit());

        String categoryName = payments.getFirst().getCategory().getDisplayName();
        long estimatedBenefit = estimateTaxBenefit(0, limitedDeductionAmount);
        return new CategorySummary(categoryCode, categoryName, spendingAmount, deductibleBaseAmount, limitedDeductionAmount, estimatedBenefit);
    }

    private long estimateTaxBenefit(long annualIncome, long incomeDeductionAmount) {
        BigDecimal marginalRate = annualIncome > 88000000
                ? BigDecimal.valueOf(0.35)
                : annualIncome > 50000000
                ? BigDecimal.valueOf(0.24)
                : annualIncome > 14000000
                ? BigDecimal.valueOf(0.15)
                : BigDecimal.valueOf(0.06);
        return BigDecimal.valueOf(incomeDeductionAmount)
                .multiply(marginalRate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    private Instant yearStart(int taxYear) {
        return LocalDate.of(taxYear, 1, 1).atStartOfDay(KOREA_ZONE).toInstant();
    }
}
