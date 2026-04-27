package com.engseg.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmailPadraoNcRequest(
        @NotNull UUID estabelecimentoId,
        @NotNull UUID empresaId,
        @NotBlank @Email String email,
        String descricao
) {}
