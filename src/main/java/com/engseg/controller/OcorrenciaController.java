package com.engseg.controller;

import com.engseg.dto.response.DesvioResponse;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.entity.Evidencia;
import com.engseg.entity.TipoEvidencia;
import com.engseg.repository.EvidenciaRepository;
import com.engseg.service.DesvioService;
import com.engseg.service.NaoConformidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ocorrencias")
@RequiredArgsConstructor
public class OcorrenciaController {

    private final DesvioService desvioService;
    private final NaoConformidadeService naoConformidadeService;
    private final EvidenciaRepository evidenciaRepository;

    private void putPrimeiraEvidencia(Map<String, Object> item, List<Evidencia> evidencias) {
        evidencias.stream().findFirst().ifPresent(e -> {
            item.put("primeiraEvidenciaId", e.getId().toString());
            item.put("primeiraEvidenciaNome", e.getNomeArquivo());
        });
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO', 'EXTERNO')")
    public ResponseEntity<List<Map<String, Object>>> listarTodas(
            @RequestParam(required = false) UUID estabelecimentoId,
            @RequestParam(required = false) UUID empresaId) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (DesvioResponse d : desvioService.findAll(estabelecimentoId, empresaId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "DESVIO");
            item.put("id", d.id());
            item.put("titulo", d.titulo());
            item.put("localizacao", d.localizacaoNome());
            item.put("descricao", d.descricao());
            item.put("dataRegistro", d.dataRegistro());
            item.put("status", d.status());
            item.put("estabelecimentoNome", d.estabelecimentoNome());
            item.put("usuarioCriacaoEmail", d.usuarioCriacaoEmail());
            putPrimeiraEvidencia(item, evidenciaRepository.findByDesvioId(d.id()));
            resultado.add(item);
        }

        for (NaoConformidadeResponse nc : naoConformidadeService.findAll(null, estabelecimentoId, empresaId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "NAO_CONFORMIDADE");
            item.put("id", nc.id());
            item.put("titulo", nc.titulo());
            item.put("localizacao", nc.localizacaoNome());
            item.put("descricao", nc.descricao());
            item.put("dataRegistro", nc.dataRegistro());
            item.put("regraDeOuro", nc.regraDeOuro());
            item.put("status", nc.status());
            item.put("dataLimiteResolucao", nc.dataLimiteResolucao());
            item.put("nivelSeveridade", nc.nivelSeveridade());
            item.put("estabelecimentoNome", nc.estabelecimentoNome());
            item.put("engResponsavelConstrutoraId", nc.engResponsavelConstrutoraId());
            item.put("engResponsavelVerificacaoId", nc.engResponsavelVerificacaoId());
            item.put("usuarioCriacaoEmail", nc.usuarioCriacaoEmail());
            item.put("vencida", nc.vencida());
            item.put("quantidadeAtividades", nc.atividades() != null ? nc.atividades().size() : 0);
            item.put("quantidadeHistorico", nc.historico() != null ? nc.historico().size() : 0);
            putPrimeiraEvidencia(item,
                    evidenciaRepository.findByNaoConformidadeIdAndTipoEvidencia(
                            nc.id(), TipoEvidencia.OCORRENCIA));
            resultado.add(item);
        }

        // sort by dataRegistro descending
        resultado.sort((a, b) -> {
            String da = String.valueOf(a.get("dataRegistro"));
            String db = String.valueOf(b.get("dataRegistro"));
            return db.compareTo(da);
        });

        return ResponseEntity.ok(resultado);
    }
}
