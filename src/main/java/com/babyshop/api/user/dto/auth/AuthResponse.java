package com.babyshop.api.user.dto.auth;

import com.babyshop.api.user.dto.user.UserProfileResponse;

/**
 * DTO for authentication response containing JWT tokens and user info.
 *
 * Design decisions:
 * - Separate access and refresh tokens for security
 * - Include token type for Bearer authentication
 * - Include expiry time for frontend token management
 * - Embed user info to avoid additional API call
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn, // in milliseconds
        UserProfileResponse user
) {
    /**
     * Factory method for creating auth response
     */
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UserProfileResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }

    /**
     * Factory method for token refresh (no user data needed)
     */
    public static AuthResponse ofRefresh(String accessToken, Long expiresIn) {
        return new AuthResponse(accessToken, null, "Bearer", expiresIn, null);
    }
}

