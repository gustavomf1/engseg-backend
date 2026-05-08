package com.engseg.event.kafka;

import java.util.UUID;

public record DesvioKafkaEvent(
        String tipo,                // "DESVIO_CRIADO" | "DESVIO_STATUS_ALTERADO"
        UUID desvioId,
        String titulo,
        String status,
        UUID responsavelId,         // responsavelDesvio
        UUID responsavelTrativaId,  // responsavelTratativa
        UUID criadorId
) {}
