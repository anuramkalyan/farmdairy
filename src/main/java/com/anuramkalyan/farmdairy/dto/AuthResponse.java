package com.anuramkalyan.farmdairy.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
