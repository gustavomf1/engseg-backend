package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EstabelecimentoRequest(
        @NotBlank String nome,
        @NotBlank String codigo,
        @NotNull UUID empresaId,
        String cidade,
        String estado
) {}
