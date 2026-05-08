package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EvidenciaResponse(
        UUID id,
        String nomeArquivo,
        String urlArquivo,
        LocalDateTime dataUpload,
        String tipoEvidencia,
        Double latitude,
        Double longitude,
        OffsetDateTime capturedAt,
        String origem,
        String cidade
) {}
