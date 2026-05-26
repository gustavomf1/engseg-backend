package com.engseg.controller;

import com.engseg.dto.response.EvidenciaResponse;
import com.engseg.entity.Evidencia;
import com.engseg.entity.TipoEvidencia;
import com.engseg.service.EvidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencias")
@RequiredArgsConstructor
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    @PostMapping("/nao-conformidade/{ncId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<EvidenciaResponse> upload(
            @PathVariable UUID ncId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "OCORRENCIA") TipoEvidencia tipo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "capturedAt", required = false) OffsetDateTime capturedAt,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "cidade", required = false) String cidade) throws IOException {
        Evidencia evidencia = evidenciaService.uploadParaNaoConformidade(ncId, file, tipo, latitude, longitude, capturedAt, origem, cidade);
        return ResponseEntity.ok(toResponse(evidencia));
    }

    @GetMapping("/nao-conformidade/{ncId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<EvidenciaResponse>> listar(
            @PathVariable UUID ncId,
            @RequestParam(value = "tipo", required = false) TipoEvidencia tipo) {
        List<EvidenciaResponse> list = evidenciaService.listarPorNaoConformidade(ncId, tipo)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/desvio/{desvioId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<EvidenciaResponse> uploadDesvio(
            @PathVariable UUID desvioId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "OCORRENCIA") TipoEvidencia tipo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "capturedAt", required = false) OffsetDateTime capturedAt,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "cidade", required = false) String cidade) throws IOException {
        Evidencia evidencia = evidenciaService.uploadParaDesvio(desvioId, file, tipo, latitude, longitude, capturedAt, origem, cidade);
        return ResponseEntity.ok(toResponse(evidencia));
    }

    @GetMapping("/desvio/{desvioId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<List<EvidenciaResponse>> listarDesvio(
            @PathVariable UUID desvioId,
            @RequestParam(value = "tipo", required = false) TipoEvidencia tipo) {
        List<EvidenciaResponse> list = evidenciaService.listarPorDesvio(desvioId, tipo)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/atividade/{atividadeId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<EvidenciaResponse> uploadAtividade(
            @PathVariable UUID atividadeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "TRATATIVA") TipoEvidencia tipo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "capturedAt", required = false) OffsetDateTime capturedAt,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "cidade", required = false) String cidade) throws IOException {
        Evidencia evidencia = evidenciaService.uploadParaAtividade(atividadeId, file, tipo, latitude, longitude, capturedAt, origem, cidade);
        return ResponseEntity.ok(toResponse(evidencia));
    }

    @GetMapping("/atividade/{atividadeId}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<List<EvidenciaResponse>> listarAtividade(@PathVariable UUID atividadeId) {
        List<EvidenciaResponse> list = evidenciaService.listarPorAtividade(atividadeId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        Evidencia evidencia = evidenciaService.buscarPorId(id);
        byte[] data = evidenciaService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + evidencia.getNomeArquivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @PatchMapping("/{id}/desvincular-atividade")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<Void> desvincularAtividade(@PathVariable UUID id) {
        evidenciaService.desvincularAtividade(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO', 'EXTERNO')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        evidenciaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EvidenciaResponse toResponse(Evidencia e) {
        return new EvidenciaResponse(
                e.getId(),
                e.getNomeArquivo(),
                e.getUrlArquivo(),
                e.getDataUpload(),
                e.getTipoEvidencia() != null ? e.getTipoEvidencia().name() : null,
                e.getLatitude(),
                e.getLongitude(),
                e.getCapturedAt(),
                e.getOrigem(),
                e.getCidade()
        );
    }
}
