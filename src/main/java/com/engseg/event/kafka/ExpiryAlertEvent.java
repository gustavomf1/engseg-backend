package com.engseg.event.kafka;

import java.util.UUID;

public record ExpiryAlertEvent(
        UUID ncId,
        String titulo,
        int diasRestantes,
        UUID responsavelId   // responsavelTratativa
) {}
