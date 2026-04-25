package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AdicionarTrativaRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotNull @NotEmpty List<UUID> evidenciaIds
) {}
