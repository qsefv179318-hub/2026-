package com.livetaxlow.taxfeedback.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentCategoryClassifier {

    public String classify(PaymentMethod method, String merchantCategoryCode) {
        if (merchantCategoryCode == null || merchantCategoryCode.isBlank()) {
            return switch (method) {
                case CREDIT -> "CREDIT_CARD";
                case DEBIT -> "DEBIT_CARD";
                case CASH -> "CASH_RECEIPT";
                default -> "NON_DEDUCTIBLE";
            };
        }

        String mcc = merchantCategoryCode.trim();
        if (mcc.startsWith("41")) {
            return "PUBLIC_TRANSPORT";
        }
        if (mcc.startsWith("54") || mcc.startsWith("59")) {
            return "TRADITIONAL_MARKET";
        }
        if (mcc.startsWith("78") || mcc.startsWith("79") || mcc.startsWith("82")) {
            return "CULTURE";
        }
        if (mcc.startsWith("80")) {
            return "MEDICAL";
        }
        return switch (method) {
            case CREDIT -> "CREDIT_CARD";
            case DEBIT -> "DEBIT_CARD";
            case CASH -> "CASH_RECEIPT";
            default -> "NON_DEDUCTIBLE";
        };
    }
}
