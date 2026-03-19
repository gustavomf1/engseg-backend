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
        String localizacao,
        String descricao,
        LocalDateTime dataRegistro,
        String tecnicoNome,
        boolean regraDeOuro,
        String nrRelacionada,
        NivelSeveridade nivelSeveridade,
        UUID engResponsavelConstrutoraId,
        String engConstrutoraEmail,
        UUID engResponsavelVerificacaoId,
        String engVerificacaoEmail,
        LocalDate dataLimiteResolucao,
        StatusNaoConformidade status,
        List<DevolutivaResponse> devolutivas,
        List<ExecucaoAcaoResponse> execucoes,
        ValidacaoResponse validacao
) {}
