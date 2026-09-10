package com.safecore.dto.response;

import java.util.List;

public record SyncBatchResponse(List<SyncItemResult> results) {}
