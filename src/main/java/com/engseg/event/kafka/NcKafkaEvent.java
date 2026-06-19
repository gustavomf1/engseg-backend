package com.engseg.event.kafka;

import java.util.List;
import java.util.UUID;

public record NcKafkaEvent(
        UUID eventId,
        String tipo,
        UUID ncId,
        List<UUID> destinatarios,
        String titulo,
        String corpo
) {}
