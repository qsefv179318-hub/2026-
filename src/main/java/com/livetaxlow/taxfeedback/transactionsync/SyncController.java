package com.livetaxlow.taxfeedback.transactionsync;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.AutoSyncResponse;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.SyncRequest;
import com.livetaxlow.taxfeedback.transactionsync.SyncDtos.SyncResponse;

import jakarta.validation.Valid;

// Receives button-triggered sync requests from the client and starts transaction sync.
@RestController
@RequestMapping("/api/transactions")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/sync")
    AutoSyncResponse syncOnMount(@RequestParam UUID userId) {
        return syncService.autoSyncAndCalculate(userId);
    }

    @PostMapping("/sync")
    SyncResponse sync(@Valid @RequestBody SyncRequest request) {
        return syncService.syncLast30Days(request.userId(), request.connectedId(), request.organization());
    }
}
