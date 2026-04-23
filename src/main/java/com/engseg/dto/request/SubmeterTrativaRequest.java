package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmeterTrativaRequest(
        @NotBlank String observacao,
        @NotNull UUID evidenciaId
) {}
