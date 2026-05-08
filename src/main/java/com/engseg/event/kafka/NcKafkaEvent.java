package com.engseg.event.kafka;

import java.time.LocalDate;
import java.util.UUID;

public record NcKafkaEvent(
        String tipo,                // "NC_CRIADA" | "NC_STATUS_ALTERADO"
        UUID ncId,
        String titulo,
        String status,
        UUID responsavelId,         // engResponsavelConstrutora
        UUID responsavelTrativaId,  // engResponsavelVerificacao
        UUID criadorId,
        LocalDate dataLimite
) {}
