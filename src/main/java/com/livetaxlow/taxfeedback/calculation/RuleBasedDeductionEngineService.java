package com.livetaxlow.taxfeedback.calculation;

import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.CategoryDeductionBreakdown;
import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.RuleBasedDeductionResult;
import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.finance.UserTransactionRepository;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleBasedDeductionEngineService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final UserService userService;
    private final UserTransactionRepository userTransactionRepository;
    private final TaxComputationParameterRepository parameterRepository;
    private final DeductionCategoryRuleRepository deductionCategoryRuleRepository;

    public RuleBasedDeductionEngineService(
            UserService userService,
            UserTransactionRepository userTransactionRepository,
            TaxComputationParameterRepository parameterRepository,
            DeductionCategoryRuleRepository deductionCategoryRuleRepository
    ) {
        this.userService = userService;
        this.userTransactionRepository = userTransactionRepository;
        this.parameterRepository = parameterRepository;
        this.deductionCategoryRuleRepository = deductionCategoryRuleRepository;
    }

    @Transactional(readOnly = true)
    public RuleBasedDeductionResult preview(UUID userId, int taxYear) {
        UserProfile user = userService.get(userId);
        Map<String, Long> spendingByCategory = userTransactionRepository.summarizeByCategory(
                userId,
                yearStart(taxYear),
                yearStart(taxYear + 1)
        ).stream().collect(Collectors.toMap(
                UserTransactionRepository.CategoryAmountView::getCategoryCode,
                row -> row.getTotalAmount() == null ? 0L : row.getTotalAmount()
        ));

        return calculate(user.getAnnualIncome(), taxYear, spendingByCategory);
    }

    @Transactional(readOnly = true)
    public RuleBasedDeductionResult calculate(long annualIncome, int taxYear, Map<String, Long> spendingByCategory) {
        RuleSet ruleSet = loadRuleSet(taxYear);

        long cardThresholdAmount = multiplyFloor(annualIncome, ruleSet.cardThresholdRate());
        long medicalThresholdAmount = multiplyFloor(annualIncome, ruleSet.medicalThresholdRate());
        long cardThresholdRemaining = cardThresholdAmount;

        long cardTotalDeduction = 0;
        List<CategoryDeductionBreakdown> breakdowns = new ArrayList<>();

        // 핵심 규칙:
        // 카드 공제 문턱(총급여의 일정 비율)을 채우는 과정에서는 공제율이 가장 낮은 항목부터 차감합니다.
        // 현재 기준에서는 신용카드(priority=1) -> 체크카드 -> 전통시장 -> 대중교통 순으로 차감됩니다.
        for (DeductionCategoryRule rule : ruleSet.cardRulesSortedByPriority()) {
            long spending = spendingByCategory.getOrDefault(rule.getCategoryCode(), 0L);
            long thresholdConsumed = Math.min(spending, cardThresholdRemaining);
            cardThresholdRemaining -= thresholdConsumed;

            long deductibleBase = Math.max(0, spending - thresholdConsumed);
            long deductionBeforeLimit = multiplyFloor(deductibleBase, rule.getDeductionRate());
            long deductionAfterLimit = applyLimit(deductionBeforeLimit, rule.getAnnualLimit());
            cardTotalDeduction += deductionAfterLimit;

            breakdowns.add(new CategoryDeductionBreakdown(
                    rule.getCategoryCode(),
                    spending,
                    thresholdConsumed,
                    deductibleBase,
                    deductionBeforeLimit,
                    deductionAfterLimit
            ));
        }

        if (ruleSet.cardTotalLimit() != null) {
            cardTotalDeduction = Math.min(cardTotalDeduction, ruleSet.cardTotalLimit());
        }

        long medicalSpend = spendingByCategory.getOrDefault("MEDICAL", 0L);
        long medicalDeductionBase = Math.max(0, medicalSpend - medicalThresholdAmount);
        long medicalDeduction = applyMedicalRate(medicalDeductionBase, ruleSet.ruleMapByCategory().get("MEDICAL"));

        long totalDeduction = cardTotalDeduction + medicalDeduction;
        long estimatedTaxReduction = multiplyFloor(totalDeduction, ruleSet.estimatedMarginalTaxRate());

        return new RuleBasedDeductionResult(
                taxYear,
                annualIncome,
                cardThresholdAmount,
                medicalThresholdAmount,
                medicalSpend,
                cardThresholdRemaining,
                cardTotalDeduction,
                medicalDeduction,
                estimatedTaxReduction,
                breakdowns
        );
    }

    private RuleSet loadRuleSet(int taxYear) {
        Map<String, BigDecimal> parameters = parameterRepository.findByTaxYear(taxYear).stream()
                .collect(Collectors.toMap(TaxComputationParameter::getParameterKey, TaxComputationParameter::getParameterValue));
        List<DeductionCategoryRule> categoryRules = deductionCategoryRuleRepository.findByTaxYearOrderByThresholdFillPriorityAsc(taxYear);
        Map<String, DeductionCategoryRule> ruleMapByCategory = categoryRules.stream()
                .collect(Collectors.toMap(DeductionCategoryRule::getCategoryCode, Function.identity()));

        BigDecimal cardThresholdRate = required(parameters, "CARD_DEDUCTION_THRESHOLD_RATE");
        BigDecimal medicalThresholdRate = required(parameters, "MEDICAL_DEDUCTION_THRESHOLD_RATE");
        BigDecimal estimatedMarginalTaxRate = required(parameters, "ESTIMATED_MARGINAL_TAX_RATE");
        Long cardTotalLimit = optionalLong(parameters, "CARD_DEDUCTION_TOTAL_LIMIT");

        List<DeductionCategoryRule> cardRules = categoryRules.stream()
                .filter(DeductionCategoryRule::isAppliesToCardThreshold)
                .sorted(Comparator.comparingInt(DeductionCategoryRule::getThresholdFillPriority))
                .toList();

        return new RuleSet(
                cardThresholdRate,
                medicalThresholdRate,
                estimatedMarginalTaxRate,
                cardTotalLimit,
                cardRules,
                ruleMapByCategory
        );
    }

    private BigDecimal required(Map<String, BigDecimal> params, String key) {
        BigDecimal value = params.get(key);
        if (value == null) {
            throw new NotFoundException("Missing tax computation parameter: " + key);
        }
        return value;
    }

    private Long optionalLong(Map<String, BigDecimal> params, String key) {
        BigDecimal value = params.get(key);
        return value == null ? null : value.setScale(0, RoundingMode.DOWN).longValue();
    }

    private long applyMedicalRate(long medicalDeductionBase, DeductionCategoryRule medicalRule) {
        if (medicalRule == null) {
            return medicalDeductionBase;
        }
        long deduction = multiplyFloor(medicalDeductionBase, medicalRule.getDeductionRate());
        return applyLimit(deduction, medicalRule.getAnnualLimit());
    }

    private long applyLimit(long value, Long limit) {
        if (limit == null || limit <= 0) {
            return value;
        }
        return Math.min(value, limit);
    }

    private long multiplyFloor(long base, BigDecimal rate) {
        return BigDecimal.valueOf(base)
                .multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    private Instant yearStart(int taxYear) {
        return LocalDate.of(taxYear, 1, 1).atStartOfDay(KOREA_ZONE).toInstant();
    }

    private record RuleSet(
            BigDecimal cardThresholdRate,
            BigDecimal medicalThresholdRate,
            BigDecimal estimatedMarginalTaxRate,
            Long cardTotalLimit,
            List<DeductionCategoryRule> cardRulesSortedByPriority,
            Map<String, DeductionCategoryRule> ruleMapByCategory
    ) {
    }
}
