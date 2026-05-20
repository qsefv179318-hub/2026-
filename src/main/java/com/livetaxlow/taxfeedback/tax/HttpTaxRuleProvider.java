package com.livetaxlow.taxfeedback.tax;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HttpTaxRuleProvider implements TaxRuleProvider {

    private final String baseUrl;
    private final WebClient webClient;

    public HttpTaxRuleProvider(@Value("${tax.rules.external-base-url:}") String baseUrl, WebClient.Builder builder) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.webClient = builder.build();
    }

    @Override
    public String providerName() {
        return "HTTP_TAX_RULES";
    }

    @Override
    public List<ExternalTaxRuleDto> fetchRules(int taxYear) {
        if (baseUrl.isBlank()) {
            return List.of();
        }

        ExternalTaxRuleDto[] rules = webClient.get()
                .uri(baseUrl + "/tax-rules?taxYear={taxYear}", taxYear)
                .retrieve()
                .bodyToMono(ExternalTaxRuleDto[].class)
                .block();

        return rules == null ? List.of() : List.of(rules);
    }
}
