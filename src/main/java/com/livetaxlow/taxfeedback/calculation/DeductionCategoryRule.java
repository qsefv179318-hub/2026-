package com.livetaxlow.taxfeedback.calculation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deduction_category_rules")
public class DeductionCategoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int taxYear;

    @Column(nullable = false)
    private String categoryCode;

    @Column(nullable = false)
    private BigDecimal deductionRate;

    private Long annualLimit;

    @Column(nullable = false)
    private int thresholdFillPriority;

    @Column(nullable = false)
    private boolean appliesToCardThreshold;

    @Column(nullable = false)
    private Instant createdAt;

    protected DeductionCategoryRule() {
    }

    public int getTaxYear() {
        return taxYear;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public BigDecimal getDeductionRate() {
        return deductionRate;
    }

    public Long getAnnualLimit() {
        return annualLimit;
    }

    public int getThresholdFillPriority() {
        return thresholdFillPriority;
    }

    public boolean isAppliesToCardThreshold() {
        return appliesToCardThreshold;
    }
}
