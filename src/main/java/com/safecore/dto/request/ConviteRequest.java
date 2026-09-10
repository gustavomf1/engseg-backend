package com.safecore.dto.request;

import com.safecore.entity.PerfilUsuario;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record ConviteRequest(
        @NotNull UUID empresaId,
        @NotNull PerfilUsuario perfil,
        @Min(1) @Max(60) int minutos
) {}
