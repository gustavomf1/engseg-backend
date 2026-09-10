package com.safecore.dto.response;

import java.util.UUID;

public record SyncItemResult(
        String localId,
        UUID serverId,
        String status,
        String erro
) {}
