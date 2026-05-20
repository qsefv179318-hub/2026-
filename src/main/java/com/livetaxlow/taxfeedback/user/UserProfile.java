package com.livetaxlow.taxfeedback.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserProfile {

    @Id
    private UUID id;

    @Column(nullable = false)
    private long annualIncome;

    @Column(nullable = false)
    private int dependentsCount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(long annualIncome, int dependentsCount) {
        this.id = UUID.randomUUID();
        this.annualIncome = annualIncome;
        this.dependentsCount = dependentsCount;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public long getAnnualIncome() {
        return annualIncome;
    }

    public int getDependentsCount() {
        return dependentsCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateIncome(long annualIncome) {
        this.annualIncome = annualIncome;
        this.updatedAt = Instant.now();
    }

    public void updateDependentsCount(int dependentsCount) {
        this.dependentsCount = dependentsCount;
        this.updatedAt = Instant.now();
    }
}
