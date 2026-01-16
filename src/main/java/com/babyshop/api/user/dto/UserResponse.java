package com.babyshop.api.user.dto;

import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user data.
 * Never exposes sensitive information like password hash.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        Boolean emailVerified,
        Boolean isActive,
        Instant lastLoginAt,
        Instant createdAt
) {
    /**
     * Factory method to create from User entity.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getEmailVerified(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}

