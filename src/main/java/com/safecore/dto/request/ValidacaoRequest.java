package com.safecore.dto.request;

import com.safecore.entity.ParecerValidacao;
import jakarta.validation.constraints.NotNull;

public record ValidacaoRequest(
        @NotNull ParecerValidacao parecer,
        String observacao
) {}
