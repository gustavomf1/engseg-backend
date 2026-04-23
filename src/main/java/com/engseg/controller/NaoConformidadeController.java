package com.engseg.controller;

import com.engseg.dto.request.AprovarRejeitarRequest;
import com.engseg.dto.request.InvestigacaoRequest;
import com.engseg.dto.request.RejeitarRequest;
import com.engseg.dto.request.NaoConformidadeRequest;
import com.engseg.dto.request.RevisarAtividadesRequest;
import com.engseg.dto.request.RevisarExecucaoRequest;
import com.engseg.dto.request.SubmeterEvidenciasRequest;
import com.engseg.dto.request.SubmeterExecucaoRequest;
import com.engseg.dto.response.HistoricoNcResponse;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<NaoConformidadeResponse>> getAll(
            @RequestParam(required = false) StatusNaoConformidade status,
            @RequestParam(required = false) UUID estabelecimentoId,
            @RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(naoConformidadeService.findAll(status, estabelecimentoId, empresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<NaoConformidadeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(naoConformidadeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> create(@Valid @RequestBody NaoConformidadeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(naoConformidadeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> update(@PathVariable UUID id, @Valid @RequestBody NaoConformidadeRequest request) {
        return ResponseEntity.ok(naoConformidadeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        naoConformidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Novo fluxo: Investigação → Plano → Execução → Validação Final
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/investigacao")
    @PreAuthorize("hasAnyRole('EXTERNO', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> submeterInvestigacao(
            @PathVariable UUID id,
            @Valid @RequestBody InvestigacaoRequest request) {
        return ResponseEntity.ok(naoConformidadeService.submeterInvestigacao(id, request));
    }

    @PostMapping("/{id}/aprovar-plano")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> aprovarPlano(
            @PathVariable UUID id,
            @RequestBody(required = false) AprovarRejeitarRequest request) {
        return ResponseEntity.ok(naoConformidadeService.aprovarPlano(id, request));
    }

    @PostMapping("/{id}/rejeitar-plano")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> rejeitarPlano(
            @PathVariable UUID id,
            @Valid @RequestBody RejeitarRequest request) {
        return ResponseEntity.ok(naoConformidadeService.rejeitarPlano(id, request));
    }

    @PostMapping("/{id}/revisar-atividades")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> revisarAtividades(
            @PathVariable UUID id,
            @Valid @RequestBody RevisarAtividadesRequest request) {
        return ResponseEntity.ok(naoConformidadeService.revisarAtividades(id, request));
    }

    @PostMapping("/{id}/submeter-execucao")
    @PreAuthorize("hasAnyRole('EXTERNO', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> submeterExecucao(
            @PathVariable UUID id,
            @Valid @RequestBody SubmeterExecucaoRequest request) {
        return ResponseEntity.ok(naoConformidadeService.submeterExecucao(id, request));
    }

    @PostMapping("/{id}/revisar-execucao")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> revisarExecucao(
            @PathVariable UUID id,
            @Valid @RequestBody RevisarExecucaoRequest request) {
        return ResponseEntity.ok(naoConformidadeService.revisarExecucao(id, request));
    }

    @PostMapping("/{id}/submeter-evidencias")
    @PreAuthorize("hasAnyRole('EXTERNO', 'TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> submeterEvidencias(
            @PathVariable UUID id,
            @Valid @RequestBody SubmeterEvidenciasRequest request) {
        return ResponseEntity.ok(naoConformidadeService.submeterEvidencias(id, request));
    }

    @PostMapping("/{id}/aprovar-evidencias")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> aprovarEvidencias(
            @PathVariable UUID id,
            @RequestBody(required = false) AprovarRejeitarRequest request) {
        return ResponseEntity.ok(naoConformidadeService.aprovarEvidencias(id, request));
    }

    @PostMapping("/{id}/rejeitar-evidencias")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NaoConformidadeResponse> rejeitarEvidencias(
            @PathVariable UUID id,
            @Valid @RequestBody RejeitarRequest request) {
        return ResponseEntity.ok(naoConformidadeService.rejeitarEvidencias(id, request));
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<HistoricoNcResponse>> getHistorico(@PathVariable UUID id) {
        return ResponseEntity.ok(naoConformidadeService.findHistorico(id));
    }

}
