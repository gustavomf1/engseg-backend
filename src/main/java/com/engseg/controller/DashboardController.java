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

        long totalNCs         = naoConformidadeRepository.countFiltered(empresaContratadaId, estabelecimentoId, empresaId);
        long abertas          = naoConformidadeRepository.countByStatusFiltered(StatusNaoConformidade.ABERTA, empresaContratadaId, estabelecimentoId, empresaId);
        long emTratamento     = naoConformidadeRepository.countByStatusFiltered(StatusNaoConformidade.EM_TRATAMENTO, empresaContratadaId, estabelecimentoId, empresaId);
        long concluidas       = naoConformidadeRepository.countByStatusFiltered(StatusNaoConformidade.CONCLUIDO, empresaContratadaId, estabelecimentoId, empresaId);
        long naoResolvidas    = naoConformidadeRepository.countByStatusFiltered(StatusNaoConformidade.NAO_RESOLVIDA, empresaContratadaId, estabelecimentoId, empresaId);
        long totalRegraDeOuro = naoConformidadeRepository.countByRegraDeOuroFiltered(true, empresaContratadaId, estabelecimentoId, empresaId);

        long totalDesvios           = desvioRepository.countFiltered(empresaContratadaId, estabelecimentoId, empresaId);
        long totalDesviosConcluidos = desvioRepository.countByStatusFiltered(StatusDesvio.CONCLUIDO, empresaContratadaId, estabelecimentoId, empresaId);
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
