package com.livetaxlow.taxfeedback.finance;

import com.livetaxlow.taxfeedback.user.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_transactions")
public class UserTransaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private ExpenseCategory category;

    @Column(nullable = false)
    private String merchantName;

    private String merchantCategoryCode;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private Instant approvedAt;

    private String externalOrderId;

    @Column(nullable = false)
    private String source;

    private String externalTxId;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserTransaction() {
    }

    public UserTransaction(
            UserProfile user,
            ExpenseCategory category,
            String merchantName,
            String merchantCategoryCode,
            String paymentMethod,
            long amount,
            Instant approvedAt,
            String externalOrderId,
            String source,
            String externalTxId
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.category = category;
        this.merchantName = merchantName;
        this.merchantCategoryCode = merchantCategoryCode;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.approvedAt = approvedAt;
        this.externalOrderId = externalOrderId;
        this.source = source;
        this.externalTxId = externalTxId;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserProfile getUser() {
        return user;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public long getAmount() {
        return amount;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getSource() {
        return source;
    }

    public String getExternalTxId() {
        return externalTxId;
    }
}
