package com.engseg.dto.request;

import com.engseg.entity.ParecerValidacao;
import jakarta.validation.constraints.NotNull;

public record ValidacaoRequest(
        @NotNull ParecerValidacao parecer,
        String observacao
) {}
