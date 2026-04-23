package com.engseg.dto.response;

import com.engseg.entity.StatusDesvio;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DesvioResponse(
        UUID id,
        UUID estabelecimentoId,
        String estabelecimentoNome,
        String titulo,
        UUID localizacaoId,
        String localizacaoNome,
        String descricao,
        LocalDateTime dataRegistro,
        String tecnicoNome,
        String usuarioCriacaoNome,
        String usuarioCriacaoEmail,
        String orientacaoRealizada,
        boolean regraDeOuro,
        StatusDesvio status,
        UUID responsavelDesvioId,
        String responsavelDesvioNome,
        UUID responsavelTratativaId,
        String responsavelTrativaNome,
        String observacaoTratativa,
        UUID evidenciaTratativaId,
        String evidenciaTrativaNome,
        String evidenciaTrativaUrl,
        List<HistoricoDesvioResponse> historico
) {}
