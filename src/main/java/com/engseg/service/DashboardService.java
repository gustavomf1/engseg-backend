package com.engseg.service;

import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DesvioRepository desvioRepository;
    private final NaoConformidadeRepository naoConformidadeRepository;

    public List<Map<String, Object>> getRecentes() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        desvioRepository.findTop10ByOrderByDataRegistroDesc().forEach(d -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "DESVIO");
            item.put("id", d.getId());
            item.put("titulo", d.getTitulo());
            item.put("localizacao", d.getLocalizacao() != null ? d.getLocalizacao().getNome() : null);
            item.put("descricao", d.getDescricao());
            item.put("dataRegistro", d.getDataRegistro());
            item.put("status", d.getStatus());
            item.put("estabelecimentoNome", d.getEstabelecimento() != null ? d.getEstabelecimento().getNome() : null);
            resultado.add(item);
        });

        naoConformidadeRepository.findTop10ByOrderByDataRegistroDesc().forEach(nc -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", "NAO_CONFORMIDADE");
            item.put("id", nc.getId());
            item.put("titulo", nc.getTitulo());
            item.put("localizacao", nc.getLocalizacao() != null ? nc.getLocalizacao().getNome() : null);
            item.put("descricao", nc.getDescricao());
            item.put("dataRegistro", nc.getDataRegistro());
            item.put("status", nc.getStatus());
            item.put("estabelecimentoNome", nc.getEstabelecimento() != null ? nc.getEstabelecimento().getNome() : null);
            resultado.add(item);
        });

        resultado.sort((a, b) -> {
            LocalDateTime da = (LocalDateTime) a.get("dataRegistro");
            LocalDateTime db = (LocalDateTime) b.get("dataRegistro");
            return db.compareTo(da);
        });

        return resultado.stream().limit(5).toList();
    }
}
