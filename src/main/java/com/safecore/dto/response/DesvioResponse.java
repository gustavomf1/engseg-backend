package com.safecore.dto.response;

import com.safecore.entity.StatusDesvio;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DesvioResponse(
        UUID id,
        String codigo,
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
        List<HistoricoDesvioResponse> historico,
        List<TrativaDesvioResponse> tratativas,
        UUID usuarioCriacaoId,
        UUID empresaContratadaId,
        String empresaContratadaNome
) {}
