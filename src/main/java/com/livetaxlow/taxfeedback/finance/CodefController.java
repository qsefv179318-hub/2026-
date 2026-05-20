package com.livetaxlow.taxfeedback.finance;

import com.livetaxlow.taxfeedback.finance.CodefTransactionSyncService.RegisterCodefLinkRequest;
import com.livetaxlow.taxfeedback.finance.CodefTransactionSyncService.SyncResult;
import com.livetaxlow.taxfeedback.finance.CodefTransactionSyncService.ConnectCodefRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/codef")
public class CodefController {

    private final CodefTransactionSyncService syncService;

    public CodefController(CodefTransactionSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/users/{userId}/links")
    LinkResponse registerLink(@PathVariable UUID userId, @Valid @RequestBody RegisterCodefLinkRequest request) {
        ExternalFinanceLink link = syncService.registerLink(userId, request);
        return new LinkResponse(link.getId(), link.getProvider(), link.getOrganization(), link.getConnectedId(), link.getClientType(), link.isActive());
    }

    @PostMapping("/users/{userId}/connect")
    LinkResponse connectAndRegister(
            @PathVariable UUID userId,
            @Valid @RequestBody ConnectCodefRequest request
    ) {
        ExternalFinanceLink link = syncService.connectAndRegisterLink(userId, request);
        return new LinkResponse(link.getId(), link.getProvider(), link.getOrganization(), link.getConnectedId(), link.getClientType(), link.isActive());
    }

    @PostMapping("/users/{userId}/sync-transactions")
    SyncResult syncTransactions(
            @PathVariable UUID userId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return syncService.syncTransactions(userId, fromDate, toDate);
    }

    public record LinkResponse(
            UUID id,
            String provider,
            String organization,
            String connectedId,
            String clientType,
            boolean active
    ) {
    }
}
