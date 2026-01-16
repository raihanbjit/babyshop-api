package com.babyshop.api.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
    /**
     * Normalize email to lowercase
     */
    public LoginRequest {
        email = email != null ? email.toLowerCase().trim() : null;
    }
}

