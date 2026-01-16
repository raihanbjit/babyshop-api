package com.babyshop.api.user.dto.user;

import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user profile response.
 * Never expose sensitive data like password hash, tokens, etc.
 */
public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        Boolean isActive,
        Boolean emailVerified,
        Instant lastLoginAt,
        Instant createdAt
) {
    /**
     * Factory method to create UserProfileResponse from User entity
     *
     * Design: Use static factory method for clean conversion
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getIsActive(),
                user.getEmailVerified(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    /**
     * Get full name
     */
    public String fullName() {
        return firstName + " " + lastName;
    }
}

