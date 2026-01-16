package com.babyshop.api.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration request.
 *
 * Validation rules:
 * - Email: valid format, required
 * - Password: min 8 chars, must contain uppercase, lowercase, digit, special char
 * - Names: 2-50 chars
 * - Phone: 10-15 digits (optional)
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        String password,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Phone number must be 10-15 digits, optionally starting with +"
        )
        String phoneNumber
) {
    /**
     * Normalize email to lowercase
     */
    public RegisterRequest {
        email = email != null ? email.toLowerCase().trim() : null;
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
        phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }
}

