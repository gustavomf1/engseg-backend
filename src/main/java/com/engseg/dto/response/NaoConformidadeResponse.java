package com.engseg.dto.response;

import com.engseg.entity.NivelSeveridade;
import com.engseg.entity.StatusNaoConformidade;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NaoConformidadeResponse(
        UUID id,
        UUID estabelecimentoId,
        String estabelecimentoNome,
        String titulo,
        UUID localizacaoId,
        String localizacaoNome,
        String descricao,
        LocalDateTime dataRegistro,
        String tecnicoNome,
        boolean regraDeOuro,
        NivelSeveridade nivelSeveridade,
        UUID engResponsavelConstrutoraId,
        String engConstruturaNome,
        String engConstrutoraEmail,
        UUID engResponsavelVerificacaoId,
        String engVerificacaoNome,
        String engVerificacaoEmail,
        LocalDate dataLimiteResolucao,
        String usuarioCriacaoNome,
        String usuarioCriacaoEmail,
        StatusNaoConformidade status,
        boolean vencida,
        boolean reincidencia,
        UUID ncAnteriorId,
        String ncAnteriorTitulo,
        List<NcResumoResponse> cadeiaReincidencias,
        List<NcResumoResponse> reincidencias,
        // Investigação — 5 Porquês (pergunta + resposta) e Causa Raiz
        String porqueUm,
        String porqueUmResposta,
        String porqueDois,
        String porqueDoisResposta,
        String porqueTres,
        String porqueTresResposta,
        String porqueQuatro,
        String porqueQuatroResposta,
        String porqueCinco,
        String porqueCincoResposta,
        String causaRaiz,
        // Execução
        String descricaoExecucao,
        // Plano de ação
        List<AtividadeResponse> atividades,
        // Histórico de decisões
        List<HistoricoNcResponse> historico,
        // Snapshots de submissões
        List<InvestigacaoSnapshotResponse> investigacaoSnapshots,
        List<ExecucaoSnapshotResponse> execucaoSnapshots,
        // Legado
        List<DevolutivaResponse> devolutivas,
        List<ExecucaoAcaoResponse> execucoes,
        List<ValidacaoResponse> validacoes,
        List<NormaResponse> normas
) {}
