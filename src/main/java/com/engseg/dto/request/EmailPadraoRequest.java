package com.engseg.dto.request;

import com.engseg.entity.TipoEmailPadrao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmailPadraoRequest(
        @NotNull UUID estabelecimentoId,
        @NotNull UUID empresaId,
        @NotBlank @Email String email,
        String descricao,
        @NotNull TipoEmailPadrao tipo
) {}
