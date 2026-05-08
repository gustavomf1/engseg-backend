package com.engseg.controller;

import com.engseg.dto.request.SyncBatchRequest;
import com.engseg.dto.response.SyncBatchResponse;
import com.engseg.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/ocorrencias")
    public ResponseEntity<SyncBatchResponse> syncOcorrencias(
            @Valid @RequestBody SyncBatchRequest request) {
        return ResponseEntity.ok(syncService.processar(request));
    }
}
