package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenciaResponse(
        UUID id,
        String nomeArquivo,
        String urlArquivo,
        LocalDateTime dataUpload
) {}
