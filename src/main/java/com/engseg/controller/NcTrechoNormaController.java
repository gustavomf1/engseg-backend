package com.engseg.controller;

import com.engseg.dto.request.NcTrechoNormaRequest;
import com.engseg.dto.response.NcTrechoNormaResponse;
import com.engseg.service.NcTrechoNormaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nao-conformidades/{ncId}/trechos-norma")
@RequiredArgsConstructor
public class NcTrechoNormaController {

    private final NcTrechoNormaService ncTrechoNormaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<NcTrechoNormaResponse>> getAll(@PathVariable UUID ncId) {
        return ResponseEntity.ok(ncTrechoNormaService.findByNc(ncId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<NcTrechoNormaResponse> vincular(
            @PathVariable UUID ncId,
            @Valid @RequestBody NcTrechoNormaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ncTrechoNormaService.vincular(ncId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<Void> deletar(@PathVariable UUID ncId, @PathVariable UUID id) {
        ncTrechoNormaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
