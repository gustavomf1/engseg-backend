package com.safecore.dto.response;

import com.safecore.entity.PerfilUsuario;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String token,
        String refreshToken,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean isAdmin
) {}
