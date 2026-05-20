package com.livetaxlow.taxfeedback.transactionsync;

import com.livetaxlow.taxfeedback.calculation.RuleBasedDeductionDtos.RuleBasedDeductionResult;
import java.time.Instant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public final class SyncDtos {

    private SyncDtos() {
    }

    public record SyncRequest(
            @NotNull UUID userId,
            @NotBlank String connectedId,
            @NotBlank String organization
    ) {
    }

    public record SyncResponse(
            LocalDate startDate,
            LocalDate endDate,
            int importedCount,
            int duplicateSkippedCount
    ) {
    }

    public record ThresholdProgress(
            long total,
            long used,
            long remaining,
            double usedRatio
    ) {
    }

    public record AutoSyncResponse(
            SyncResponse sync,
            RuleBasedDeductionResult deduction,
            ThresholdProgress cardThreshold,
            ThresholdProgress medicalThreshold,
            SyncFeedback feedback
    ) {
    }

    public record SyncFeedback(
            String type,
            String title,
            String message,
            long expectedBenefit,
            Instant createdAt
    ) {
    }
}
