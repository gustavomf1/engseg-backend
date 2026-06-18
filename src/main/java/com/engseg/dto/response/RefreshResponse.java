package com.engseg.dto.response;

public record RefreshResponse(
        String token,
        String refreshToken
) {}
