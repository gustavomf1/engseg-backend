package com.engseg.controller;

import com.engseg.dto.request.*;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<DesvioResponse>> getAll(
            @RequestParam(required = false) UUID estabelecimentoId,
            @RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(desvioService.findAll(estabelecimentoId, empresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<DesvioResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(desvioService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> create(@Valid @RequestBody DesvioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desvioService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> update(@PathVariable UUID id, @Valid @RequestBody DesvioRequest request) {
        return ResponseEntity.ok(desvioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        desvioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tratativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<DesvioResponse> adicionarTratativa(
            @PathVariable UUID id,
            @Valid @RequestBody AdicionarTrativaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desvioService.adicionarTratativa(id, request));
    }

    @DeleteMapping("/{id}/tratativas/{trativaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<Void> removerTratativa(
            @PathVariable UUID id,
            @PathVariable UUID trativaId) {
        desvioService.removerTratativa(id, trativaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submeter-tratativa")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<DesvioResponse> submeterTratativa(
            @PathVariable UUID id,
            @RequestBody(required = false) SubmeterTrativaDesvioRequest request) {
        return ResponseEntity.ok(desvioService.submeterTratativa(id, request));
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> aprovar(
            @PathVariable UUID id,
            @Valid @RequestBody AprovarDesvioRequest request) {
        return ResponseEntity.ok(desvioService.aprovar(id, request));
    }

    @PostMapping("/{id}/reprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DesvioResponse> reprovar(
            @PathVariable UUID id,
            @Valid @RequestBody ReprovarTrativasDesvioRequest request) {
        return ResponseEntity.ok(desvioService.reprovar(id, request));
    }
}
