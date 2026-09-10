package com.safecore.dto.request;

public record SyncItemRequest(
        String localId,
        String tipo,
        NaoConformidadeRequest nc,
        DesvioRequest desvio
) {}
