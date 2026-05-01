package com.engseg.controller;

import com.engseg.dto.request.EmailPadraoNcRequest;
import com.engseg.dto.response.EmailPadraoNcResponse;
import com.engseg.service.EmailPadraoNcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails-padrao-nc")
@RequiredArgsConstructor
public class EmailPadraoNcController {

    private final EmailPadraoNcService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmailPadraoNcResponse>> listar(
            @RequestParam UUID estabelecimentoId,
            @RequestParam UUID empresaId) {
        return ResponseEntity.ok(service.listar(estabelecimentoId, empresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailPadraoNcResponse> criar(
            @Valid @RequestBody EmailPadraoNcRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PatchMapping("/{id}/descricao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailPadraoNcResponse> atualizarDescricao(
            @PathVariable UUID id,
            @RequestBody String descricao) {
        return ResponseEntity.ok(service.atualizarDescricao(id, descricao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
