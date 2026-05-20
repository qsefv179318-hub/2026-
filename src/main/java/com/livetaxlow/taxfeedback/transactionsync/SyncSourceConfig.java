package com.livetaxlow.taxfeedback.transactionsync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetaxlow.taxfeedback.finance.CodefClient;
import com.livetaxlow.taxfeedback.finance.CodefProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SyncSourceProperties.class)
public class SyncSourceConfig {

    @Bean
    TransactionSource transactionSource(
            SyncSourceProperties properties,
            CodefClient codefClient,
            CodefProperties codefProperties,
            ObjectMapper objectMapper
    ) {
        String type = properties.type() == null ? "codef" : properties.type().trim().toLowerCase();
        if ("mock".equals(type)) {
            return new MockTransactionSource(objectMapper, properties);
        }
        return new CodefTransactionSource(codefClient, codefProperties);
    }
}

