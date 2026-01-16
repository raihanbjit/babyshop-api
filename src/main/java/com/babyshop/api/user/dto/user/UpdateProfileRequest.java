package com.babyshop.api.user.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile.
 *
 * Security note: Email and role cannot be changed via this endpoint
 */
public record UpdateProfileRequest(

        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Phone number must be 10-15 digits, optionally starting with +"
        )
        String phoneNumber
) {
    /**
     * Compact constructor for normalization
     */
    public UpdateProfileRequest {
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
        phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }
}

