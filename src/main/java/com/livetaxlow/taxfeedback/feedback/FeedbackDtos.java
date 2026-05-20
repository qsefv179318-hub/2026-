package com.livetaxlow.taxfeedback.feedback;

import java.time.Instant;
import java.util.UUID;

public final class FeedbackDtos {

    private FeedbackDtos() {
    }

    public record FeedbackResponse(
            UUID id,
            UUID paymentId,
            FeedbackType type,
            String title,
            String message,
            long expectedBenefit,
            Instant createdAt
    ) {
        static FeedbackResponse from(Feedback feedback) {
            UUID paymentId = feedback.getPayment() == null ? null : feedback.getPayment().getId();
            return new FeedbackResponse(
                    feedback.getId(),
                    paymentId,
                    feedback.getType(),
                    feedback.getTitle(),
                    feedback.getMessage(),
                    feedback.getExpectedBenefit(),
                    feedback.getCreatedAt()
            );
        }
    }
}
