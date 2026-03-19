package com.engseg.dto.response;

import java.util.UUID;

public record EstabelecimentoResponse(
        UUID id,
        String nome,
        String codigo,
        UUID empresaId,
        String empresaNome,
        String cidade,
        String estado,
        boolean ativo
) {}
