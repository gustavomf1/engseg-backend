package com.engseg.controller;

import com.engseg.dto.request.LocalizacaoRequest;
import com.engseg.dto.response.LocalizacaoResponse;
import com.engseg.service.LocalizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/localizacoes")
@RequiredArgsConstructor
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<List<LocalizacaoResponse>> getAll(
            @RequestParam(required = false) UUID estabelecimentoId,
            @RequestParam(required = false) Boolean ativo) {
        if (estabelecimentoId != null) {
            return ResponseEntity.ok(localizacaoService.findByEstabelecimentoId(estabelecimentoId));
        }
        return ResponseEntity.ok(localizacaoService.findAll(ativo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<LocalizacaoResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(localizacaoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocalizacaoResponse> create(@Valid @RequestBody LocalizacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(localizacaoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocalizacaoResponse> update(@PathVariable UUID id, @Valid @RequestBody LocalizacaoRequest request) {
        return ResponseEntity.ok(localizacaoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        localizacaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocalizacaoResponse> reativar(@PathVariable UUID id) {
        return ResponseEntity.ok(localizacaoService.reativar(id));
    }
}
