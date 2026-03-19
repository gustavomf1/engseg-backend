package com.engseg.controller;

import com.engseg.dto.request.DevolutivaRequest;
import com.engseg.dto.request.ExecucaoAcaoRequest;
import com.engseg.dto.request.NaoConformidadeRequest;
import com.engseg.dto.request.ValidacaoRequest;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.service.NaoConformidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nao-conformidades")
@RequiredArgsConstructor
public class NaoConformidadeController {

    private final NaoConformidadeService naoConformidadeService;

    @GetMapping
    public ResponseEntity<List<NaoConformidadeResponse>> getAll(
            @RequestParam(required = false) StatusNaoConformidade status,
            @RequestParam(required = false) UUID estabelecimentoId) {
        return ResponseEntity.ok(naoConformidadeService.findAll(status, estabelecimentoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NaoConformidadeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(naoConformidadeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> create(@Valid @RequestBody NaoConformidadeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(naoConformidadeService.create(request));
    }

    @PostMapping("/{id}/devolutiva")
    @PreAuthorize("hasRole('EXTERNO')")
    public ResponseEntity<NaoConformidadeResponse> registrarDevolutiva(
            @PathVariable UUID id,
            @Valid @RequestBody DevolutivaRequest request) {
        return ResponseEntity.ok(naoConformidadeService.registrarDevolutiva(id, request));
    }

    @PostMapping("/{id}/execucao-acao")
    @PreAuthorize("hasRole('EXTERNO')")
    public ResponseEntity<NaoConformidadeResponse> registrarExecucaoAcao(
            @PathVariable UUID id,
            @Valid @RequestBody ExecucaoAcaoRequest request) {
        return ResponseEntity.ok(naoConformidadeService.registrarExecucaoAcao(id, request));
    }

    @PostMapping("/{id}/validacao")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> validar(
            @PathVariable UUID id,
            @Valid @RequestBody ValidacaoRequest request) {
        return ResponseEntity.ok(naoConformidadeService.validar(id, request));
    }
}
