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
@Table(name = "tax_computation_parameters")
public class TaxComputationParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int taxYear;

    @Column(nullable = false)
    private String parameterKey;

    @Column(nullable = false)
    private BigDecimal parameterValue;

    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    protected TaxComputationParameter() {
    }

    public int getTaxYear() {
        return taxYear;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public BigDecimal getParameterValue() {
        return parameterValue;
    }
}
