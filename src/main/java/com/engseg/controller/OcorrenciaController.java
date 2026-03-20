package com.engseg.controller;

import com.engseg.dto.response.DesvioResponse;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.service.DesvioService;
import com.engseg.service.NaoConformidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ocorrencias")
@RequiredArgsConstructor
public class OcorrenciaController {

    private final DesvioService desvioService;
    private final NaoConformidadeService naoConformidadeService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarTodas() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (DesvioResponse d : desvioService.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "DESVIO");
            item.put("id", d.id());
            item.put("titulo", d.titulo());
            item.put("localizacao", d.localizacaoNome());
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
            item.put("localizacao", nc.localizacaoNome());
            item.put("descricao", nc.descricao());
            item.put("dataRegistro", nc.dataRegistro());
            item.put("regraDeOuro", nc.regraDeOuro());
            item.put("status", nc.status());
            item.put("dataLimiteResolucao", nc.dataLimiteResolucao());
            item.put("nrRelacionada", nc.nrRelacionada());
            item.put("nivelSeveridade", nc.nivelSeveridade());
            item.put("estabelecimentoNome", nc.estabelecimentoNome());
            item.put("engResponsavelConstrutoraId", nc.engResponsavelConstrutoraId());
            item.put("engResponsavelVerificacaoId", nc.engResponsavelVerificacaoId());
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
