package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExecucaoAcaoResponse(
        UUID id,
        String descricaoAcaoExecutada,
        LocalDateTime dataExecucao,
        String engenheiroNome
) {}
