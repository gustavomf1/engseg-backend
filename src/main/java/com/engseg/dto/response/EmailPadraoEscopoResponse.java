package com.engseg.dto.response;

import java.util.UUID;

public record EmailPadraoEscopoResponse(
        UUID estabelecimentoId,
        String estabelecimentoNome,
        UUID empresaId,
        String empresaNome,
        int emailCount
) {}
