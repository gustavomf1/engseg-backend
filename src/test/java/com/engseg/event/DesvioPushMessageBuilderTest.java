package com.engseg.event;

import com.engseg.entity.*;
import com.engseg.event.kafka.DesvioKafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.engseg.entity.StatusDesvio.*;
import static org.assertj.core.api.Assertions.assertThat;

class DesvioPushMessageBuilderTest {

    private DesvioPushMessageBuilder builder;
    private Desvio desvio;

    private final UUID criadorId = UUID.randomUUID();
    private final UUID responsavelDesvioId = UUID.randomUUID();
    private final UUID responsavelTratativaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        builder = new DesvioPushMessageBuilder();

        Usuario criador = new Usuario(); criador.setId(criadorId);
        Usuario responsavelDesvio = new Usuario(); responsavelDesvio.setId(responsavelDesvioId);
        Usuario responsavelTratativa = new Usuario(); responsavelTratativa.setId(responsavelTratativaId);

        desvio = new Desvio();
        desvio.setId(UUID.randomUUID());
        desvio.setTitulo("Desvio de Teste");
        desvio.setUsuarioCriacao(criador);
        desvio.setResponsavelDesvio(responsavelDesvio);
        desvio.setResponsavelTratativa(responsavelTratativa);
    }

    @Test
    void aberto_para_aguardandoTratativa_gera_DESVIO_ATIVADO_com_tratativa_e_desvio() {
        DesvioKafkaEvent event = builder.resolver(desvio, ABERTO, AGUARDANDO_TRATATIVA);

        assertThat(event).isNotNull();
        assertThat(event.tipo()).isEqualTo("DESVIO_ATIVADO");
        assertThat(event.destinatarios()).containsExactly(responsavelTratativaId, responsavelDesvioId);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.desvioId()).isEqualTo(desvio.getId());
        assertThat(event.titulo()).isEqualTo("EngSeg — Desvio ativado");
        assertThat(event.corpo()).contains("Desvio de Teste");
    }

    @Test
    void aguardandoTratativa_para_aguardandoAprovacao_gera_DESVIO_TRATATIVA_SUBMETIDA_com_desvio_e_criador() {
        DesvioKafkaEvent event = builder.resolver(desvio, AGUARDANDO_TRATATIVA, AGUARDANDO_APROVACAO);

        assertThat(event).isNotNull();
        assertThat(event.tipo()).isEqualTo("DESVIO_TRATATIVA_SUBMETIDA");
        assertThat(event.destinatarios()).containsExactly(responsavelDesvioId, criadorId);
    }

    @Test
    void aguardandoAprovacao_para_concluido_gera_DESVIO_APROVADO_com_tratativa_e_criador() {
        DesvioKafkaEvent event = builder.resolver(desvio, AGUARDANDO_APROVACAO, CONCLUIDO);

        assertThat(event).isNotNull();
        assertThat(event.tipo()).isEqualTo("DESVIO_APROVADO");
        assertThat(event.destinatarios()).containsExactly(responsavelTratativaId, criadorId);
    }

    @Test
    void aguardandoAprovacao_para_aguardandoTratativa_gera_DESVIO_REPROVADO_com_tratativa_e_criador() {
        DesvioKafkaEvent event = builder.resolver(desvio, AGUARDANDO_APROVACAO, AGUARDANDO_TRATATIVA);

        assertThat(event).isNotNull();
        assertThat(event.tipo()).isEqualTo("DESVIO_REPROVADO");
        assertThat(event.destinatarios()).containsExactly(responsavelTratativaId, criadorId);
    }

    @Test
    void criacao_aberto_sem_statusAnterior_retorna_null() {
        assertThat(builder.resolver(desvio, null, ABERTO)).isNull();
    }

    @Test
    void transicao_nao_mapeada_retorna_null() {
        // ABERTO -> CONCLUIDO não existe no fluxo normal mas resolver deve retornar null
        assertThat(builder.resolver(desvio, ABERTO, CONCLUIDO)).isNull();
    }

    @Test
    void quando_responsavel_tratativa_ausente_DESVIO_ATIVADO_notifica_so_responsavel_desvio() {
        desvio.setResponsavelTratativa(null);
        DesvioKafkaEvent event = builder.resolver(desvio, ABERTO, AGUARDANDO_TRATATIVA);

        assertThat(event).isNotNull();
        assertThat(event.destinatarios()).containsExactly(responsavelDesvioId);
    }

    @Test
    void quando_todos_destinatarios_ausentes_retorna_null() {
        desvio.setResponsavelTratativa(null);
        desvio.setResponsavelDesvio(null);
        assertThat(builder.resolver(desvio, ABERTO, AGUARDANDO_TRATATIVA)).isNull();
    }
}
