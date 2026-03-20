package com.engseg.dto.response;

import java.util.UUID;

public record LocalizacaoResponse(
        UUID id,
        String nome,
        UUID estabelecimentoId,
        String estabelecimentoNome,
        boolean ativo
) {}
