package com.engseg.controller;

import com.engseg.dto.request.EmailPadraoRequest;
import com.engseg.dto.response.EmailPadraoResponse;
import com.engseg.entity.TipoEmailPadrao;
import com.engseg.service.EmailPadraoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails-padrao")
@RequiredArgsConstructor
public class EmailPadraoController {

    private final EmailPadraoService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmailPadraoResponse>> listar(
            @RequestParam UUID estabelecimentoId,
            @RequestParam UUID empresaId,
            @RequestParam TipoEmailPadrao tipo) {
        return ResponseEntity.ok(service.listar(estabelecimentoId, empresaId, tipo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailPadraoResponse> criar(
            @Valid @RequestBody EmailPadraoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PatchMapping("/{id}/descricao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailPadraoResponse> atualizarDescricao(
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
