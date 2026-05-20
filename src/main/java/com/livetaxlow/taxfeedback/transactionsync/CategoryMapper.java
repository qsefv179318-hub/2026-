package com.livetaxlow.taxfeedback.transactionsync;

import org.springframework.stereotype.Component;

// Maps CODEF store type text into the app's internal transaction categories.
@Component
public class CategoryMapper {

    public InternalTransactionCategory mapFromStoreType(String storeType) {
        if (storeType == null || storeType.isBlank()) {
            return InternalTransactionCategory.GENERAL;
        }

        String normalized = storeType.toLowerCase();

        if (containsAny(normalized, "병원", "의원", "약국", "의료", "치과", "한의원")) {
            return InternalTransactionCategory.MEDICAL;
        }
        if (containsAny(normalized, "교통", "버스", "지하철", "철도", "택시", "주유")) {
            return InternalTransactionCategory.TRANSPORT;
        }
        if (containsAny(normalized, "음식", "식당", "외식", "카페", "제과", "패스트푸드", "푸드")) {
            return InternalTransactionCategory.FOOD;
        }

        return InternalTransactionCategory.GENERAL;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
