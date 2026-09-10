package com.safecore.dto.response;

public record RefreshResponse(
        String token,
        String refreshToken
) {}
