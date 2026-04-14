package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejeitarRequest(
        @NotBlank(message = "Motivo é obrigatório") String motivo
) {}
