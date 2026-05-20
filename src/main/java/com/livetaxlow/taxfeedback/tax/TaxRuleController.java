package com.livetaxlow.taxfeedback.tax;

import com.livetaxlow.taxfeedback.tax.TaxRuleDtos.SyncTaxRulesResponse;
import com.livetaxlow.taxfeedback.tax.TaxRuleDtos.TaxRuleResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax-rules")
public class TaxRuleController {

    private final TaxRuleService taxRuleService;

    public TaxRuleController(TaxRuleService taxRuleService) {
        this.taxRuleService = taxRuleService;
    }

    @GetMapping
    List<TaxRuleResponse> list(@RequestParam(required = false) Integer taxYear) {
        int targetYear = taxYear == null ? LocalDate.now().getYear() : taxYear;
        return taxRuleService.list(targetYear).stream()
                .map(TaxRuleResponse::from)
                .toList();
    }

    @PostMapping("/sync")
    SyncTaxRulesResponse sync(@RequestParam(required = false) Integer taxYear) {
        int targetYear = taxYear == null ? LocalDate.now().getYear() : taxYear;
        return taxRuleService.sync(targetYear);
    }
}
