package com.engseg.controller;

import com.engseg.dto.response.DashboardStatsResponse;
import com.engseg.entity.StatusDesvio;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final DesvioRepository desvioRepository;
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<DashboardStatsResponse> getStats(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID estabelecimentoId,
            @RequestParam(required = false) UUID empresaContratadaId) {
        long totalNCs, abertas, emTratamento, concluidas, naoResolvidas, totalRegraDeOuro;

        if (empresaContratadaId != null) {
            totalNCs         = naoConformidadeRepository.countByEmpresaContratadaId(empresaContratadaId);
            abertas          = naoConformidadeRepository.countByStatusAndEmpresaContratadaId(StatusNaoConformidade.ABERTA, empresaContratadaId);
            emTratamento     = naoConformidadeRepository.countByStatusAndEmpresaContratadaId(StatusNaoConformidade.EM_TRATAMENTO, empresaContratadaId);
            concluidas       = naoConformidadeRepository.countByStatusAndEmpresaContratadaId(StatusNaoConformidade.CONCLUIDO, empresaContratadaId);
            naoResolvidas    = naoConformidadeRepository.countByStatusAndEmpresaContratadaId(StatusNaoConformidade.NAO_RESOLVIDA, empresaContratadaId);
            totalRegraDeOuro = naoConformidadeRepository.countByRegraDeOuroAndEmpresaContratadaId(true, empresaContratadaId);
        } else {
            totalNCs         = naoConformidadeRepository.count();
            abertas          = naoConformidadeRepository.countByStatus(StatusNaoConformidade.ABERTA);
            emTratamento     = naoConformidadeRepository.countByStatus(StatusNaoConformidade.EM_TRATAMENTO);
            concluidas       = naoConformidadeRepository.countByStatus(StatusNaoConformidade.CONCLUIDO);
            naoResolvidas    = naoConformidadeRepository.countByStatus(StatusNaoConformidade.NAO_RESOLVIDA);
            totalRegraDeOuro = naoConformidadeRepository.countByRegraDeOuro(true);
        }

        long totalDesvios           = desvioRepository.count();
        long totalDesviosConcluidos = desvioRepository.countByStatus(StatusDesvio.CONCLUIDO);
        long totalOcorrencias       = totalDesvios + totalNCs;

        return ResponseEntity.ok(new DashboardStatsResponse(
                totalOcorrencias,
                totalDesvios,
                totalNCs,
                totalRegraDeOuro,
                abertas,
                emTratamento,
                concluidas,
                naoResolvidas,
                totalDesviosConcluidos
        ));
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO')")
    public ResponseEntity<List<Map<String, Object>>> getRecentes() {
        return ResponseEntity.ok(dashboardService.getRecentes());
    }
}
