package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NcTrechoNormaResponse(
        UUID id,
        UUID normaId,
        String normaTitulo,
        String clausulaReferencia,
        String textoEditado,
        LocalDateTime dataVinculo
) {}
