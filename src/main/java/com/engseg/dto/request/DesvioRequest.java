package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DesvioRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        @NotBlank String localizacao,
        @NotBlank String descricao,
        @NotBlank String orientacaoRealizada,
        boolean regraDeOuro
) {}
