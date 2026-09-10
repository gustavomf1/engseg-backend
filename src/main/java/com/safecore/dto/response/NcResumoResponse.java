package com.safecore.dto.response;

import com.safecore.entity.StatusNaoConformidade;
import java.time.LocalDateTime;
import java.util.UUID;

public record NcResumoResponse(
        UUID id,
        String codigo,
        String titulo,
        LocalDateTime dataRegistro,
        StatusNaoConformidade status
) {}
