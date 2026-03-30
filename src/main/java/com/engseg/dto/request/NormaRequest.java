package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NormaRequest(
        @NotBlank String titulo,
        String descricao,
        String conteudo
) {}
