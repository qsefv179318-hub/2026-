package com.livetaxlow.taxfeedback.tax;

import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.tax.TaxRuleDtos.SyncTaxRulesResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxRuleService {

    private final TaxRuleRepository taxRuleRepository;
    private final TaxCategoryRepository taxCategoryRepository;
    private final TaxRuleSyncLogRepository syncLogRepository;
    private final TaxRuleProvider taxRuleProvider;

    public TaxRuleService(
            TaxRuleRepository taxRuleRepository,
            TaxCategoryRepository taxCategoryRepository,
            TaxRuleSyncLogRepository syncLogRepository,
            TaxRuleProvider taxRuleProvider
    ) {
        this.taxRuleRepository = taxRuleRepository;
        this.taxCategoryRepository = taxCategoryRepository;
        this.syncLogRepository = syncLogRepository;
        this.taxRuleProvider = taxRuleProvider;
    }

    @Transactional(readOnly = true)
    public List<TaxRule> list(int taxYear) {
        return taxRuleRepository.findByTaxYear(taxYear);
    }

    @Transactional
    public SyncTaxRulesResponse sync(int taxYear) {
        List<ExternalTaxRuleDto> externalRules = taxRuleProvider.fetchRules(taxYear);
        int count = 0;

        for (ExternalTaxRuleDto externalRule : externalRules) {
            TaxCategory category = taxCategoryRepository.findByCode(externalRule.categoryCode())
                    .orElseThrow(() -> new NotFoundException("Tax category not found: " + externalRule.categoryCode()));

            TaxRule taxRule = taxRuleRepository
                    .findByTaxYearAndCategory_Code(externalRule.taxYear(), externalRule.categoryCode())
                    .orElseGet(() -> new TaxRule(
                            externalRule.taxYear(),
                            category,
                            externalRule.deductionRate(),
                            externalRule.baseLimit(),
                            externalRule.additionalLimit(),
                            externalRule.minIncomeThresholdRate(),
                            externalRule.incomeLimit(),
                            externalRule.source(),
                            externalRule.effectiveFrom() == null ? LocalDate.of(externalRule.taxYear(), 1, 1) : externalRule.effectiveFrom()
                    ));

            taxRule.updateFrom(
                    externalRule.deductionRate(),
                    externalRule.baseLimit(),
                    externalRule.additionalLimit(),
                    externalRule.minIncomeThresholdRate(),
                    externalRule.incomeLimit(),
                    externalRule.source(),
                    externalRule.effectiveFrom() == null ? LocalDate.of(externalRule.taxYear(), 1, 1) : externalRule.effectiveFrom(),
                    externalRule.effectiveTo()
            );
            taxRuleRepository.save(taxRule);
            count++;
        }

        String message = count == 0
                ? "No external tax rules fetched. Check TAX_RULES_EXTERNAL_BASE_URL if sync is expected."
                : "External tax rules synced.";
        syncLogRepository.save(new TaxRuleSyncLog(taxRuleProvider.providerName(), taxYear, "SUCCESS", message));
        return new SyncTaxRulesResponse(taxRuleProvider.providerName(), taxYear, count, "SUCCESS", message);
    }
}
