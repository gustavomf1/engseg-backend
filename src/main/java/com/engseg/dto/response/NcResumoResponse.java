package com.engseg.dto.response;

import com.engseg.entity.StatusNaoConformidade;
import java.time.LocalDateTime;
import java.util.UUID;

public record NcResumoResponse(
        UUID id,
        String titulo,
        LocalDateTime dataRegistro,
        StatusNaoConformidade status
) {}
