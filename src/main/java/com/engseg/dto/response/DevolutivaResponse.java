package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DevolutivaResponse(
        UUID id,
        String descricaoPlanoAcao,
        LocalDateTime dataDevolutiva,
        String engenheiroNome
) {}
