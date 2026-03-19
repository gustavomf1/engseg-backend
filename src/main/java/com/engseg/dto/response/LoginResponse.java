package com.engseg.dto.response;

import com.engseg.entity.PerfilUsuario;

public record LoginResponse(
        String token,
        String nome,
        PerfilUsuario perfil
) {}
