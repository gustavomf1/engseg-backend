package com.safecore.dto.response;

import com.safecore.entity.PerfilUsuario;
import java.time.LocalDate;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        PerfilUsuario perfil,
        UUID empresaId,
        String empresaNome,
        String empresaCnpj,
        String telefone,
        boolean ativo,
        boolean isAdmin,
        LocalDate dtCriacao,
        LocalDate dtInativacao
) {}
