package com.livetaxlow.taxfeedback.tax;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tax_rules")
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int taxYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private TaxCategory category;

    @Column(nullable = false)
    private BigDecimal deductionRate;

    @Column(nullable = false)
    private long baseLimit;

    @Column(nullable = false)
    private long additionalLimit;

    @Column(nullable = false)
    private BigDecimal minIncomeThresholdRate;

    private Long incomeLimit;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false)
    private Instant createdAt;

    protected TaxRule() {
    }

    public TaxRule(
            int taxYear,
            TaxCategory category,
            BigDecimal deductionRate,
            long baseLimit,
            long additionalLimit,
            BigDecimal minIncomeThresholdRate,
            Long incomeLimit,
            String source,
            LocalDate effectiveFrom
    ) {
        this.taxYear = taxYear;
        this.category = category;
        this.deductionRate = deductionRate;
        this.baseLimit = baseLimit;
        this.additionalLimit = additionalLimit;
        this.minIncomeThresholdRate = minIncomeThresholdRate;
        this.incomeLimit = incomeLimit;
        this.source = source;
        this.effectiveFrom = effectiveFrom;
        this.createdAt = Instant.now();
    }

    public void updateFrom(
            BigDecimal deductionRate,
            long baseLimit,
            long additionalLimit,
            BigDecimal minIncomeThresholdRate,
            Long incomeLimit,
            String source,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this.deductionRate = deductionRate;
        this.baseLimit = baseLimit;
        this.additionalLimit = additionalLimit;
        this.minIncomeThresholdRate = minIncomeThresholdRate;
        this.incomeLimit = incomeLimit;
        this.source = source;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public Long getId() {
        return id;
    }

    public int getTaxYear() {
        return taxYear;
    }

    public TaxCategory getCategory() {
        return category;
    }

    public BigDecimal getDeductionRate() {
        return deductionRate;
    }

    public long getBaseLimit() {
        return baseLimit;
    }

    public long getAdditionalLimit() {
        return additionalLimit;
    }

    public BigDecimal getMinIncomeThresholdRate() {
        return minIncomeThresholdRate;
    }

    public Long getIncomeLimit() {
        return incomeLimit;
    }

    public String getSource() {
        return source;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
}
