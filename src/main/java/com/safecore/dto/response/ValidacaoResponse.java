package com.safecore.dto.response;

import com.safecore.entity.ParecerValidacao;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidacaoResponse(
        UUID id,
        ParecerValidacao parecer,
        String observacao,
        LocalDateTime dataValidacao,
        String engenheiroNome
) {}
