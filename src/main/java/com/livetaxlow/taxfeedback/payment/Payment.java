package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.tax.TaxCategory;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    private String tossOrderId;

    private String paymentKey;

    @Column(nullable = false)
    private String merchantName;

    private String merchantCategoryCode;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private TaxCategory category;

    @Column(name = "is_deductible", nullable = false)
    private boolean deductible;

    @Column(nullable = false)
    private Instant approvedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode rawPayload;

    protected Payment() {
    }

    public Payment(
            UserProfile user,
            String tossOrderId,
            String paymentKey,
            String merchantName,
            String merchantCategoryCode,
            long amount,
            PaymentMethod method,
            PaymentStatus status,
            TaxCategory category,
            boolean deductible,
            Instant approvedAt,
            JsonNode rawPayload
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.tossOrderId = tossOrderId;
        this.paymentKey = paymentKey;
        this.merchantName = merchantName;
        this.merchantCategoryCode = merchantCategoryCode;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.category = category;
        this.deductible = deductible;
        this.approvedAt = approvedAt;
        this.createdAt = Instant.now();
        this.rawPayload = rawPayload;
    }

    public UUID getId() {
        return id;
    }

    public UserProfile getUser() {
        return user;
    }

    public String getTossOrderId() {
        return tossOrderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public long getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public TaxCategory getCategory() {
        return category;
    }

    public boolean isDeductible() {
        return deductible;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}
