package com.engseg.event;

import com.engseg.entity.AtividadePlanoAcao;
import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.entity.Usuario;
import com.engseg.event.kafka.NcKafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NcPushMessageBuilderTest {

    private final NcPushMessageBuilder builder = new NcPushMessageBuilder();

    private NaoConformidade nc;
    private Usuario criador;
    private Usuario responsavelNc;
    private Usuario responsavelTratativa;

    @BeforeEach
    void setup() {
        criador = new Usuario();
        criador.setId(UUID.randomUUID());

        responsavelNc = new Usuario();
        responsavelNc.setId(UUID.randomUUID());

        responsavelTratativa = new Usuario();
        responsavelTratativa.setId(UUID.randomUUID());

        nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("Vazamento na linha 3");
        nc.setUsuarioCriacao(criador);
        nc.setResponsavelNc(responsavelNc);
        nc.setResponsavelTratativa(responsavelTratativa);
    }

    @Test
    void criacao_naoNotificaNinguem() {
        NcKafkaEvent evento = builder.resolver(nc, null, StatusNaoConformidade.ABERTA, null);
        assertThat(evento.tipo()).isEqualTo("NC_CRIADA");
        assertThat(evento.destinatarios()).isEmpty();
    }

    @Test
    void ativacao_notificaResponsavelTratativa() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.ABERTA, StatusNaoConformidade.AGUARDANDO_TRATATIVA, null);
        assertThat(evento.tipo()).isEqualTo("NC_ATIVADA");
        assertThat(evento.destinatarios()).containsExactly(responsavelTratativa.getId());
    }

    @Test
    void submissaoPlano_aPartirDeAguardandoTratativa_notificaResponsavelNcECriador() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_TRATATIVA, StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, null);
        assertThat(evento.tipo()).isEqualTo("NC_PLANO_SUBMETIDO");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(responsavelNc.getId(), criador.getId());
    }

    @Test
    void reenvioPlano_aPartirDeEmAjustePeloExterno_tambemNotificaComoSubmissao() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO, StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, null);
        assertThat(evento.tipo()).isEqualTo("NC_PLANO_SUBMETIDO");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(responsavelNc.getId(), criador.getId());
    }

    @Test
    void aprovacaoPlano_notificaResponsavelTratativaECriador_comCorpoGenerico() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_EXECUCAO, null);
        assertThat(evento.tipo()).isEqualTo("NC_PLANO_APROVADO");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(responsavelTratativa.getId(), criador.getId());
        assertThat(evento.corpo()).contains("todas as atividades aprovadas");
    }

    @Test
    void reprovacaoPlano_comMotivoPorAtividade_enumeraAtividades() {
        nc.setAtividades(List.of(
                atividade("Isolar a área", "APROVADA", null),
                atividade("Treinar equipe", "REJEITADA", "faltou cronograma")
        ));

        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO,
                "motivo geral");

        assertThat(evento.tipo()).isEqualTo("NC_PLANO_REPROVADO");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(responsavelTratativa.getId(), criador.getId());
        assertThat(evento.corpo())
                .contains("✅ Isolar a área aprovada")
                .contains("❌ Treinar equipe reprovada — faltou cronograma");
    }

    @Test
    void reprovacaoPlano_semMotivoPorAtividade_metodoLegado_usaComentarioGeral() {
        // rejeitarPlano (legado) marca status=REJEITADA mas nunca preenche motivoRejeicao por atividade.
        nc.setAtividades(List.of(atividade("Isolar a área", "REJEITADA", null)));

        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO,
                "motivo geral da rejeição");

        assertThat(evento.tipo()).isEqualTo("NC_PLANO_REPROVADO");
        assertThat(evento.corpo()).contains("motivo geral da rejeição");
        assertThat(evento.corpo()).doesNotContain("✅").doesNotContain("❌");
    }

    @Test
    void execucaoSubmetida_notificaResponsavelNcECriador() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.EM_EXECUCAO, StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, null);
        assertThat(evento.tipo()).isEqualTo("NC_EXECUCAO_SUBMETIDA");
        assertThat(evento.destinatarios()).containsExactlyInAnyOrder(responsavelNc.getId(), criador.getId());
    }

    @Test
    void conclusao_notificaOsTresEnvolvidos() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.CONCLUIDO, null);
        assertThat(evento.tipo()).isEqualTo("NC_CONCLUIDA");
        assertThat(evento.destinatarios())
                .containsExactlyInAnyOrder(criador.getId(), responsavelNc.getId(), responsavelTratativa.getId());
        assertThat(evento.corpo()).contains("todas as atividades aprovadas");
    }

    @Test
    void validacaoReprovada_notificaSoResponsavelTratativa_eEnumeraAtividadesDeExecucao() {
        AtividadePlanoAcao aprovada = atividade("Isolar a área", "APROVADA", null);
        aprovada.setStatusExecucao("APROVADA");
        AtividadePlanoAcao reprovada = atividade("Treinar equipe", "APROVADA", null);
        reprovada.setStatusExecucao("REJEITADA");
        reprovada.setMotivoRejeicaoExecucao("evidência ilegível");
        nc.setAtividades(List.of(aprovada, reprovada));

        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.EM_EXECUCAO,
                "motivo geral da execução");

        assertThat(evento.tipo()).isEqualTo("NC_VALIDACAO_REPROVADA");
        assertThat(evento.destinatarios()).containsExactly(responsavelTratativa.getId());
        assertThat(evento.corpo())
                .contains("✅ Isolar a área aprovada")
                .contains("❌ Treinar equipe reprovada — evidência ilegível");
    }

    @Test
    void leDoParCorreto_quandoAtividadeTemAmbasAsFasesPreenchidas() {
        // Atividade já passou pela fase de plano (aprovada, sem motivo) e está reprovada na execução:
        // o builder deve ler status/motivoRejeicao OU statusExecucao/motivoRejeicaoExecucao conforme a fase do evento, nunca o par errado.
        AtividadePlanoAcao atividade = atividade("Isolar a área", "APROVADA", null);
        atividade.setStatusExecucao("REJEITADA");
        atividade.setMotivoRejeicaoExecucao("evidência fora do padrão");
        nc.setAtividades(List.of(atividade));

        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.EM_EXECUCAO, null);

        assertThat(evento.corpo()).contains("❌ Isolar a área reprovada — evidência fora do padrão");
        assertThat(evento.corpo()).doesNotContain("aprovada — ");
    }

    @Test
    void parDeStatusNaoMapeado_retornaNull() {
        NcKafkaEvent evento = builder.resolver(nc,
                StatusNaoConformidade.ABERTA, StatusNaoConformidade.EM_TRATAMENTO, null);
        assertThat(evento).isNull();
    }

    private AtividadePlanoAcao atividade(String titulo, String status, String motivoRejeicao) {
        return AtividadePlanoAcao.builder()
                .id(UUID.randomUUID())
                .titulo(titulo)
                .descricao("desc")
                .ordem(1)
                .status(status)
                .motivoRejeicao(motivoRejeicao)
                .build();
    }
}
