package com.engseg.controller;

import com.engseg.dto.request.DesvioRequest;
import com.engseg.dto.response.DesvioResponse;
import com.engseg.service.DesvioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/desvios")
@RequiredArgsConstructor
public class DesvioController {

    private final DesvioService desvioService;

    @GetMapping
    public ResponseEntity<List<DesvioResponse>> getAll() {
        return ResponseEntity.ok(desvioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesvioResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(desvioService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> create(@Valid @RequestBody DesvioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desvioService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> update(@PathVariable UUID id, @Valid @RequestBody DesvioRequest request) {
        return ResponseEntity.ok(desvioService.update(id, request));
    }
}
