package com.engseg.service;

import com.engseg.entity.Desvio;
import com.engseg.entity.Estabelecimento;
import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock DesvioRepository desvioRepository;
    @Mock NaoConformidadeRepository naoConformidadeRepository;

    @InjectMocks
    DashboardService service;

    private Estabelecimento buildEstabelecimento() {
        Estabelecimento est = new Estabelecimento();
        est.setId(UUID.randomUUID());
        est.setNome("Est. Teste");
        return est;
    }

    private Desvio buildDesvio(LocalDateTime dataRegistro) {
        Desvio d = new Desvio();
        d.setId(UUID.randomUUID());
        d.setTitulo("Desvio Teste");
        d.setDescricao("Descrição");
        d.setDataRegistro(dataRegistro);
        d.setEstabelecimento(buildEstabelecimento());
        return d;
    }

    private NaoConformidade buildNc(LocalDateTime dataRegistro) {
        Estabelecimento est = buildEstabelecimento();
        NaoConformidade nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("NC Teste");
        nc.setDescricao("Descrição NC");
        nc.setDataRegistro(dataRegistro);
        nc.setStatus(StatusNaoConformidade.ABERTA);
        nc.setEstabelecimento(est);
        return nc;
    }

    @Test
    void getRecentes_retornaNoMaximo5Itens() {
        List<Desvio> desvios = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            desvios.add(buildDesvio(LocalDateTime.now().minusDays(i)));
        }
        when(desvioRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(desvios);
        when(naoConformidadeRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of());

        List<Map<String, Object>> resultado = service.getRecentes();

        assertThat(resultado).hasSize(5);
    }

    @Test
    void getRecentes_ordenadoPorDataRegistroDecrescente() {
        LocalDateTime mais_recente = LocalDateTime.now();
        LocalDateTime mais_antigo = LocalDateTime.now().minusDays(5);

        Desvio desvioRecente = buildDesvio(mais_recente);
        NaoConformidade ncAntiga = buildNc(mais_antigo);

        when(desvioRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of(desvioRecente));
        when(naoConformidadeRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of(ncAntiga));

        List<Map<String, Object>> resultado = service.getRecentes();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).get("tipo")).isEqualTo("DESVIO");
        assertThat(resultado.get(1).get("tipo")).isEqualTo("NAO_CONFORMIDADE");
    }

    @Test
    void getRecentes_quandoSemOcorrencias_retornaListaVazia() {
        when(desvioRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of());
        when(naoConformidadeRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of());

        List<Map<String, Object>> resultado = service.getRecentes();

        assertThat(resultado).isEmpty();
    }

    @Test
    void getRecentes_mapContemCamposEsperados() {
        Desvio desvio = buildDesvio(LocalDateTime.now());
        when(desvioRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of(desvio));
        when(naoConformidadeRepository.findTop10ByOrderByDataRegistroDesc()).thenReturn(List.of());

        List<Map<String, Object>> resultado = service.getRecentes();

        assertThat(resultado).hasSize(1);
        Map<String, Object> item = resultado.get(0);
        assertThat(item).containsKeys("tipo", "id", "titulo", "descricao", "dataRegistro", "status", "estabelecimentoNome");
        assertThat(item.get("tipo")).isEqualTo("DESVIO");
        assertThat(item.get("titulo")).isEqualTo("Desvio Teste");
    }
}
