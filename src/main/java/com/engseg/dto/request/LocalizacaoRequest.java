package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LocalizacaoRequest(
        @NotBlank String nome,
        @NotNull UUID estabelecimentoId
) {}
