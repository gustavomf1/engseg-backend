package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BuscarTrechoRequest(
        @NotBlank String prompt
) {}
