package com.engseg.event;

import com.engseg.entity.AtividadePlanoAcao;
import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.event.kafka.NcKafkaEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.engseg.entity.StatusNaoConformidade.*;

@Component
public class NcPushMessageBuilder {

    public NcKafkaEvent resolver(NaoConformidade nc, StatusNaoConformidade statusAnterior,
                                  StatusNaoConformidade statusNovo, String comentario) {
        UUID criadorId = nc.getUsuarioCriacao() != null ? nc.getUsuarioCriacao().getId() : null;
        UUID responsavelNcId = nc.getResponsavelNc() != null ? nc.getResponsavelNc().getId() : null;
        UUID responsavelTratativaId = nc.getResponsavelTratativa() != null ? nc.getResponsavelTratativa().getId() : null;

        String tipo;
        Set<UUID> destinatarios = new LinkedHashSet<>();

        if (statusAnterior == null && statusNovo == ABERTA) {
            tipo = "NC_CRIADA";
        } else if (statusAnterior == ABERTA && statusNovo == AGUARDANDO_TRATATIVA) {
            tipo = "NC_ATIVADA";
            addIfPresent(destinatarios, responsavelTratativaId);
        } else if ((statusAnterior == AGUARDANDO_TRATATIVA || statusAnterior == EM_AJUSTE_PELO_EXTERNO)
                && statusNovo == AGUARDANDO_APROVACAO_PLANO) {
            // Cobre a submissão inicial (AGUARDANDO_TRATATIVA) e o reenvio do plano corrigido
            // após reprovação (EM_AJUSTE_PELO_EXTERNO) — mesma ação de negócio, mesmo tipo.
            tipo = "NC_PLANO_SUBMETIDO";
            addIfPresent(destinatarios, responsavelNcId);
            addIfPresent(destinatarios, criadorId);
        } else if (statusAnterior == AGUARDANDO_APROVACAO_PLANO && statusNovo == EM_EXECUCAO) {
            tipo = "NC_PLANO_APROVADO";
            addIfPresent(destinatarios, responsavelTratativaId);
            addIfPresent(destinatarios, criadorId);
        } else if (statusAnterior == AGUARDANDO_APROVACAO_PLANO && statusNovo == EM_AJUSTE_PELO_EXTERNO) {
            tipo = "NC_PLANO_REPROVADO";
            addIfPresent(destinatarios, responsavelTratativaId);
            addIfPresent(destinatarios, criadorId);
        } else if (statusAnterior == EM_EXECUCAO && statusNovo == AGUARDANDO_VALIDACAO_FINAL) {
            tipo = "NC_EXECUCAO_SUBMETIDA";
            addIfPresent(destinatarios, responsavelNcId);
            addIfPresent(destinatarios, criadorId);
        } else if (statusAnterior == AGUARDANDO_VALIDACAO_FINAL && statusNovo == CONCLUIDO) {
            tipo = "NC_CONCLUIDA";
            addIfPresent(destinatarios, criadorId);
            addIfPresent(destinatarios, responsavelNcId);
            addIfPresent(destinatarios, responsavelTratativaId);
        } else if (statusAnterior == AGUARDANDO_VALIDACAO_FINAL && statusNovo == EM_EXECUCAO) {
            tipo = "NC_VALIDACAO_REPROVADA";
            addIfPresent(destinatarios, responsavelTratativaId);
        } else {
            return null;
        }

        String titulo = "EngSeg — " + nc.getTitulo();
        String corpo = montarCorpo(nc, tipo, comentario);
        return new NcKafkaEvent(UUID.randomUUID(), tipo, nc.getId(), List.copyOf(destinatarios), titulo, corpo);
    }

    private void addIfPresent(Set<UUID> destinatarios, UUID id) {
        if (id != null) destinatarios.add(id);
    }

    private String montarCorpo(NaoConformidade nc, String tipo, String comentario) {
        String titulo = nc.getTitulo();
        return switch (tipo) {
            case "NC_CRIADA" -> "Nova NC aberta: \"" + titulo + "\".";
            case "NC_ATIVADA" -> "\"" + titulo + "\" está aguardando sua tratativa.";
            case "NC_PLANO_SUBMETIDO" -> "Plano de ação submetido para aprovação: \"" + titulo + "\".";
            case "NC_PLANO_APROVADO" -> "Plano da NC \"" + titulo + "\": todas as atividades aprovadas.";
            case "NC_PLANO_REPROVADO" -> corpoRevisao(nc, comentario, false);
            case "NC_EXECUCAO_SUBMETIDA" -> "Execução submetida para validação: \"" + titulo + "\".";
            case "NC_CONCLUIDA" -> "NC \"" + titulo + "\" concluída: todas as atividades aprovadas.";
            case "NC_VALIDACAO_REPROVADA" -> corpoRevisao(nc, comentario, true);
            default -> titulo;
        };
    }

    private String corpoRevisao(NaoConformidade nc, String comentario, boolean faseExecucao) {
        String titulo = nc.getTitulo();
        List<AtividadePlanoAcao> atividades = nc.getAtividades();

        boolean temMotivoNaFase = atividades.stream().anyMatch(a ->
                faseExecucao ? a.getMotivoRejeicaoExecucao() != null : a.getMotivoRejeicao() != null);

        String rotulo = faseExecucao ? "Validação final" : "Plano";

        if (!temMotivoNaFase) {
            // Veio de um método legado (rejeitarPlano/rejeitarEvidencias) que não preenche
            // motivo por atividade — usa só o comentário geral da rejeição, sem enumerar.
            return rotulo + " da NC \"" + titulo + "\" reprovado" +
                    (comentario != null && !comentario.isBlank() ? ": " + comentario : ".");
        }

        String detalhe = atividades.stream()
                .map(a -> {
                    String status = faseExecucao ? a.getStatusExecucao() : a.getStatus();
                    String motivo = faseExecucao ? a.getMotivoRejeicaoExecucao() : a.getMotivoRejeicao();
                    return "REJEITADA".equals(status)
                            ? "❌ " + a.getTitulo() + " reprovada" + (motivo != null ? " — " + motivo : "")
                            : "✅ " + a.getTitulo() + " aprovada";
                })
                .collect(Collectors.joining(" · "));
        return rotulo + " da NC \"" + titulo + "\": " + detalhe;
    }
}
