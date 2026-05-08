package com.engseg.dto.response;

import java.util.List;

public record SyncBatchResponse(List<SyncItemResult> results) {}
