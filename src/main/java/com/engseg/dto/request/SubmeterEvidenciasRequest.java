package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SubmeterEvidenciasRequest(
        @NotBlank String descricaoExecucao,
        List<String> emailsManuais
) {}
