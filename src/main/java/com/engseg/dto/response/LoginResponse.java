package com.engseg.dto.response;

import com.engseg.entity.PerfilUsuario;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String token,
        String nome,
        String email,
        PerfilUsuario perfil
) {}
