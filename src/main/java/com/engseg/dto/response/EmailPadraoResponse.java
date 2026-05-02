package com.engseg.dto.response;

import java.util.UUID;

public record EmailPadraoResponse(
        UUID id,
        UUID estabelecimentoId,
        String estabelecimentoNome,
        UUID empresaId,
        String empresaNome,
        String email,
        String descricao
) {}
