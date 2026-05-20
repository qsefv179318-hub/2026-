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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transaction_import_runs")
public class TransactionImportRun {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private int importedCount;

    @Column(nullable = false)
    private int skippedCount;

    @Column(nullable = false)
    private int failedCount;

    private String message;

    protected TransactionImportRun() {
    }

    public TransactionImportRun(UserProfile user, String provider, LocalDate fromDate, LocalDate toDate) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.provider = provider;
        this.startedAt = Instant.now();
        this.status = "RUNNING";
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.importedCount = 0;
        this.skippedCount = 0;
        this.failedCount = 0;
    }

    public void addImported() {
        importedCount++;
    }

    public void addSkipped() {
        skippedCount++;
    }

    public void addFailed() {
        failedCount++;
    }

    public void finishSuccess(String message) {
        this.status = "SUCCESS";
        this.finishedAt = Instant.now();
        this.message = message;
    }

    public void finishFailed(String message) {
        this.status = "FAILED";
        this.finishedAt = Instant.now();
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public String getMessage() {
        return message;
    }
}
