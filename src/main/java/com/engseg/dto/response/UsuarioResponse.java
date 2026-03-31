package com.engseg.dto.response;

import com.engseg.entity.PerfilUsuario;
import java.time.LocalDate;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        PerfilUsuario perfil,
        UUID empresaId,
        String empresaNome,
        String telefone,
        boolean ativo,
        LocalDate dtInativacao
) {}
