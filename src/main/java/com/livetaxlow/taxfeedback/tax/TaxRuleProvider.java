package com.livetaxlow.taxfeedback.tax;

import java.util.List;

public interface TaxRuleProvider {

    String providerName();

    List<ExternalTaxRuleDto> fetchRules(int taxYear);
}
