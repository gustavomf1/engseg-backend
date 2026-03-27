package com.engseg.dto.response;

import java.util.UUID;

public record AtividadeResponse(
        UUID id,
        String descricao,
        Integer ordem
) {}
