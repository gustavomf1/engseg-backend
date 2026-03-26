package com.engseg.dto.response;

import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String razaoSocial,
        String cnpj,
        String nomeFantasia,
        String email,
        String telefone,
        UUID empresaMaeId,
        String empresaMaeNome,
        boolean ativo
) {}
