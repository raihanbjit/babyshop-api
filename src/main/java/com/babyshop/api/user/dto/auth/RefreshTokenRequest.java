package com.babyshop.api.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refresh token request.
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}

