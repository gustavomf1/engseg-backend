package com.engseg.controller;

import com.engseg.dto.request.RelatorioFiltroRequest;
import com.engseg.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private static final MediaType XLSX =
        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final RelatorioService relatorioService;

    @GetMapping("/ncs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> relatorioNcs(RelatorioFiltroRequest filtro) throws Exception {
        return xlsxResponse(relatorioService.gerarRelatorioNcs(filtro), "relatorio-ncs.xlsx");
    }

    @GetMapping("/desvios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> relatorioDesvios(RelatorioFiltroRequest filtro) throws Exception {
        return xlsxResponse(relatorioService.gerarRelatorioDesvios(filtro), "relatorio-desvios.xlsx");
    }

    @GetMapping("/ncs-vencidas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> relatorioNcsVencidas(RelatorioFiltroRequest filtro) throws Exception {
        return xlsxResponse(relatorioService.gerarRelatorioNcsVencidas(filtro), "relatorio-ncs-vencidas.xlsx");
    }

    @GetMapping("/resumo-empresa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> relatorioResumoEmpresa(RelatorioFiltroRequest filtro) throws Exception {
        return xlsxResponse(relatorioService.gerarResumoEmpresa(filtro), "relatorio-resumo-empresa.xlsx");
    }

    private ResponseEntity<byte[]> xlsxResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
            .contentType(XLSX)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .body(bytes);
    }
}
