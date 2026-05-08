package com.engseg.dto.response;

import java.util.UUID;

public record SyncItemResult(
        String localId,
        UUID serverId,
        String status,   // "CRIADO" | "ERRO"
        String erro
) {}
