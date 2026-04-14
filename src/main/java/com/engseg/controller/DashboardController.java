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
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        long totalDesvios = desvioRepository.count();
        long totalNCs = naoConformidadeRepository.count();
        long totalOcorrencias = totalDesvios + totalNCs;
        long totalRegraDeOuro = naoConformidadeRepository.countByRegraDeOuro(true);
        long abertas = naoConformidadeRepository.countByStatus(StatusNaoConformidade.ABERTA);
        long emTratamento = naoConformidadeRepository.countByStatus(StatusNaoConformidade.EM_TRATAMENTO);
        long concluidas = naoConformidadeRepository.countByStatus(StatusNaoConformidade.CONCLUIDO);
        long naoResolvidas = naoConformidadeRepository.countByStatus(StatusNaoConformidade.NAO_RESOLVIDA);
        long totalDesviosConcluidos = desvioRepository.countByStatus(StatusDesvio.CONCLUIDO);

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
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<List<Map<String, Object>>> getRecentes() {
        return ResponseEntity.ok(dashboardService.getRecentes());
    }
}
