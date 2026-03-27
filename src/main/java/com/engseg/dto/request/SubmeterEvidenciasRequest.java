package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmeterEvidenciasRequest(
        @NotBlank String descricaoExecucao
) {}
