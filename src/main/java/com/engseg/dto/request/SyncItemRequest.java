package com.engseg.dto.request;

public record SyncItemRequest(
        String localId,
        String tipo,                    // "NC" | "DESVIO"
        NaoConformidadeRequest nc,      // presente quando tipo=NC
        DesvioRequest desvio            // presente quando tipo=DESVIO
) {}
