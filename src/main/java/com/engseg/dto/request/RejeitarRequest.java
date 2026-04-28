package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RejeitarRequest(
        @NotBlank(message = "Motivo é obrigatório") String motivo,
        List<String> emailsManuais
) {}
