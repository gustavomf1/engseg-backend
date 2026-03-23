package com.engseg.dto.response;

import com.engseg.entity.StatusDesvio;
import java.time.LocalDateTime;
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
        String orientacaoRealizada,
        StatusDesvio status
) {}
