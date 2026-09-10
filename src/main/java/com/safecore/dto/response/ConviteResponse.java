package com.safecore.dto.response;

import com.safecore.entity.PerfilUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConviteResponse(
        UUID token,
        String empresaNome,
        String empresaCnpj,
        PerfilUsuario perfil,
        LocalDateTime expiresAt
) {}
