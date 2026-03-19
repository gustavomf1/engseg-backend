package com.engseg.dto.request;

import com.engseg.entity.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String senha,
        @NotNull PerfilUsuario perfil,
        @NotNull UUID empresaId,
        String telefone
) {}
