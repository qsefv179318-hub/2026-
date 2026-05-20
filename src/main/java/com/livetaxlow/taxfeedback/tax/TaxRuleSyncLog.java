package com.livetaxlow.taxfeedback.tax;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tax_rule_sync_logs")
public class TaxRuleSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private int taxYear;

    @Column(nullable = false)
    private String status;

    private String message;

    @Column(nullable = false)
    private Instant syncedAt;

    protected TaxRuleSyncLog() {
    }

    public TaxRuleSyncLog(String provider, int taxYear, String status, String message) {
        this.provider = provider;
        this.taxYear = taxYear;
        this.status = status;
        this.message = message;
        this.syncedAt = Instant.now();
    }
}
