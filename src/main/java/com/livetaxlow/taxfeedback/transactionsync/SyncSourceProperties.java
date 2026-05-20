package com.livetaxlow.taxfeedback.transactionsync;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sync.source")
public record SyncSourceProperties(
        String type,
        String mockFile
) {
}

