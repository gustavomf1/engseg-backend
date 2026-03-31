package com.engseg.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record NormaResponse(
        UUID id,
        String titulo,
        String descricao,
        String conteudo,
        boolean ativo,
        LocalDate dtInativacao
) {}
