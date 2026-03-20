package com.engseg.controller;

import com.engseg.dto.response.DashboardStatsResponse;
import com.engseg.dto.response.DesvioResponse;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.entity.StatusDesvio;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.DesvioService;
import com.engseg.service.NaoConformidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final DesvioRepository desvioRepository;
    private final DesvioService desvioService;
    private final NaoConformidadeService naoConformidadeService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        long totalDesvios = desvioRepository.count();
        long totalNCs = naoConformidadeRepository.count();
        long totalOcorrencias = totalDesvios + totalNCs;
        long totalRegraDeOuro = desvioRepository.countByRegraDeOuro(true)
                + naoConformidadeRepository.countByRegraDeOuro(true);
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
    public ResponseEntity<List<Map<String, Object>>> getRecentes() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (DesvioResponse d : desvioService.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "DESVIO");
            item.put("id", d.id());
            item.put("titulo", d.titulo());
            item.put("localizacao", d.localizacao());
            item.put("descricao", d.descricao());
            item.put("dataRegistro", d.dataRegistro());
            item.put("regraDeOuro", d.regraDeOuro());
            item.put("status", d.status());
            item.put("estabelecimentoNome", d.estabelecimentoNome());
            resultado.add(item);
        }

        for (NaoConformidadeResponse nc : naoConformidadeService.findAll(null, null)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "NAO_CONFORMIDADE");
            item.put("id", nc.id());
            item.put("titulo", nc.titulo());
            item.put("localizacao", nc.localizacao());
            item.put("descricao", nc.descricao());
            item.put("dataRegistro", nc.dataRegistro());
            item.put("regraDeOuro", nc.regraDeOuro());
            item.put("status", nc.status());
            item.put("dataLimiteResolucao", nc.dataLimiteResolucao());
            item.put("nrRelacionada", nc.nrRelacionada());
            item.put("nivelSeveridade", nc.nivelSeveridade());
            item.put("estabelecimentoNome", nc.estabelecimentoNome());
            resultado.add(item);
        }

        resultado.sort((a, b) -> {
            String da = String.valueOf(a.get("dataRegistro"));
            String db = String.valueOf(b.get("dataRegistro"));
            return db.compareTo(da);
        });

        return ResponseEntity.ok(resultado.stream().limit(5).toList());
    }
}
