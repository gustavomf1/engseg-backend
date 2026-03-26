package com.engseg.dto.response;

import java.util.UUID;

public record EstabelecimentoEmpresaResponse(
        UUID id,
        UUID estabelecimentoId,
        String estabelecimentoNome,
        UUID empresaId,
        String empresaNome,
        boolean ativo
) {}
