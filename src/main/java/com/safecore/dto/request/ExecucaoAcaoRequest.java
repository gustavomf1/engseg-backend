package com.safecore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExecucaoAcaoRequest(
        @NotBlank String descricaoAcaoExecutada
) {}
