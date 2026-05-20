package com.livetaxlow.taxfeedback.finance;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codef")
public record CodefProperties(
        boolean enabled,
        String baseUrl,
        String tokenPath,
        String accountCreatePath,
        String transactionPath,
        String clientId,
        String clientSecret,
        String publicKey,
        String responseListPath,
        FieldMapping fieldMapping
) {
    public record FieldMapping(
            String txId,
            String txDate,
            String txTime,
            String amount,
            String merchant,
            String mcc,
            String paymentMethod
    ) {
    }
}
