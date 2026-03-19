package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DevolutivaRequest(
        @NotBlank String descricaoPlanoAcao
) {}
