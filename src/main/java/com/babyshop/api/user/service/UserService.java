package com.babyshop.api.user.service;

import com.babyshop.api.common.exception.BusinessException;
import com.babyshop.api.common.exception.ResourceNotFoundException;
import com.babyshop.api.user.dto.user.ChangePasswordRequest;
import com.babyshop.api.user.dto.user.UpdateProfileRequest;
import com.babyshop.api.user.dto.user.UserProfileResponse;
import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * User management service implementation.
 *
 * Design decisions:
 * - Get authenticated user from SecurityContext
 * - Prevent privilege escalation (users can only modify their own data)
 * - Validate password before allowing change
 * - Use @Transactional for data consistency
 * - Never expose sensitive data in responses
 *
 * Security:
 * - Only authenticated users can access
 * - Users can only modify their own profile
 * - Password validation required for password change
 * - Email and role cannot be changed via profile update
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get current authenticated user's profile.
     *
     * @return user profile
     * @throws ResourceNotFoundException if user not found
     */
    public UserProfileResponse getProfile() {
        User user = getCurrentUser();
        log.debug("Fetching profile for user ID: {}", user.getId());
        return UserProfileResponse.from(user);
    }

    /**
     * Update current user's profile.
     *
     * Security note:
     * - Email cannot be changed (security risk)
     * - Role cannot be changed (privilege escalation risk)
     * - Only non-null fields are updated (partial update support)
     *
     * @param request update request with optional fields
     * @return updated user profile
     */
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        log.info("Updating profile for user ID: {}", user.getId());

        // Update only non-null fields (partial update)
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName());
        }

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        user = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", user.getId());

        return UserProfileResponse.from(user);
    }

    /**
     * Change user password with current password validation.
     *
     * Security requirements:
     * 1. Must verify current password before change
     * 2. New password must be different from current
     * 3. New password must match confirmation
     * 4. Password must meet complexity requirements (validated in DTO)
     *
     * @param request password change request
     * @throws BusinessException if validation fails
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        log.info("Password change requested for user ID: {}", user.getId());

        // Validate password confirmation
        if (!request.isPasswordConfirmed()) {
            throw new BusinessException(
                    "New password and confirmation do not match",
                    "PASSWORD_MISMATCH",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("Password change failed - incorrect current password for user ID: {}", user.getId());
            throw new BusinessException(
                    "Current password is incorrect",
                    "INVALID_PASSWORD",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate new password is different
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException(
                    "New password must be different from current password",
                    "PASSWORD_UNCHANGED",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", user.getId());
    }

    /**
     * Get currently authenticated user from SecurityContext.
     *
     * Design: Centralized method to get current user
     *
     * @return current authenticated user
     * @throws ResourceNotFoundException if user not authenticated or not found
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Get current user's ID.
     *
     * @return current user ID
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Find user by ID (for internal use).
     *
     * @param id user ID
     * @return user entity
     * @throws ResourceNotFoundException if user not found
     */
    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .filter(user -> !user.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

