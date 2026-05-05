package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EmpresaRequest(
        @NotBlank String razaoSocial,
        @NotBlank @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres") String cnpj,
        String nomeFantasia,
        @Size(max = 100) String email,
        @Size(max = 20) String telefone,
        UUID empresaMaeId,
        Boolean exibirNoSeletor
) {}
