package com.livetaxlow.taxfeedback.feedback;

import com.livetaxlow.taxfeedback.payment.Payment;

public interface AiFeedbackGenerator {

    AiFeedback generate(Payment payment, FeedbackType fallbackType, String fallbackTitle, String fallbackMessage, long expectedBenefit);

    record AiFeedback(
            FeedbackType type,
            String title,
            String message
    ) {
    }
}
