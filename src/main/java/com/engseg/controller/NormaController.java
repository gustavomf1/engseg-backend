package com.engseg.controller;

import com.engseg.dto.request.BuscarTrechoRequest;
import com.engseg.dto.request.NormaRequest;
import com.engseg.dto.response.BuscarTrechoResponse;
import com.engseg.dto.response.NormaResponse;
import com.engseg.service.NormaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/normas")
@RequiredArgsConstructor
public class NormaController {

    private final NormaService normaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<List<NormaResponse>> getAll(
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(normaService.findAll(ativo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<NormaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(normaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> create(@Valid @RequestBody NormaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(normaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> update(@PathVariable UUID id, @Valid @RequestBody NormaRequest request) {
        return ResponseEntity.ok(normaService.update(id, request));
    }

    @PutMapping("/{id}/conteudo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> salvarConteudo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(normaService.salvarConteudo(id, body.get("conteudo")));
    }

    @PostMapping("/{id}/buscar-trecho")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<BuscarTrechoResponse> buscarTrecho(
            @PathVariable UUID id,
            @Valid @RequestBody BuscarTrechoRequest request) {
        return ResponseEntity.ok(normaService.buscarTrecho(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        normaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NormaResponse> reativar(@PathVariable UUID id) {
        return ResponseEntity.ok(normaService.reativar(id));
    }
}
