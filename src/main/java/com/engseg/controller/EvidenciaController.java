package com.engseg.controller;

import com.engseg.entity.Evidencia;
import com.engseg.entity.TipoEvidencia;
import com.engseg.service.EvidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencias")
@RequiredArgsConstructor
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    @PostMapping("/nao-conformidade/{ncId}")
    public ResponseEntity<EvidenciaResponse> upload(
            @PathVariable UUID ncId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "OCORRENCIA") TipoEvidencia tipo) throws IOException {
        Evidencia evidencia = evidenciaService.uploadParaNaoConformidade(ncId, file, tipo);
        return ResponseEntity.ok(toResponse(evidencia));
    }

    @GetMapping("/nao-conformidade/{ncId}")
    public ResponseEntity<List<EvidenciaResponse>> listar(
            @PathVariable UUID ncId,
            @RequestParam(value = "tipo", required = false) TipoEvidencia tipo) {
        List<EvidenciaResponse> list = evidenciaService.listarPorNaoConformidade(ncId, tipo)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        Evidencia evidencia = evidenciaService.buscarPorId(id);
        byte[] data = evidenciaService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + evidencia.getNomeArquivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        evidenciaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EvidenciaResponse toResponse(Evidencia e) {
        return new EvidenciaResponse(
                e.getId().toString(),
                e.getNomeArquivo(),
                e.getUrlArquivo(),
                e.getDataUpload().toString(),
                e.getTipoEvidencia().name()
        );
    }

    public record EvidenciaResponse(String id, String nomeArquivo, String urlArquivo, String dataUpload, String tipoEvidencia) {}
}
