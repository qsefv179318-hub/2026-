package com.livetaxlow.taxfeedback.feedback;

import com.livetaxlow.taxfeedback.payment.Payment;
import com.livetaxlow.taxfeedback.user.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private long expectedBenefit;

    @Column(nullable = false)
    private Instant createdAt;

    protected Feedback() {
    }

    public Feedback(UserProfile user, Payment payment, FeedbackType type, String title, String message, long expectedBenefit) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.payment = payment;
        this.type = type;
        this.title = title;
        this.message = message;
        this.expectedBenefit = expectedBenefit;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public FeedbackType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getExpectedBenefit() {
        return expectedBenefit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
