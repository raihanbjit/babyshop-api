package com.babyshop.api.user.controller;

import com.babyshop.api.common.dto.ApiResponse;
import com.babyshop.api.user.dto.user.ChangePasswordRequest;
import com.babyshop.api.user.dto.user.UpdateProfileRequest;
import com.babyshop.api.user.dto.user.UserProfileResponse;
import com.babyshop.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller.
 *
 * All endpoints require authentication.
 *
 * Endpoints:
 * - GET /api/v1/users/profile - Get current user profile
 * - PUT /api/v1/users/profile - Update current user profile
 * - PUT /api/v1/users/change-password - Change password
 *
 * Security:
 * - @PreAuthorize ensures only authenticated users can access
 * - Users can only modify their own data (enforced in service layer)
 * - No admin privilege required for these endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current authenticated user's profile.
     *
     * @return user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        log.debug("Get profile request received");

        UserProfileResponse response = userService.getProfile();

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", response));
    }

    /**
     * Update current user's profile.
     *
     * Security note: Email and role cannot be changed via this endpoint
     *
     * @param request update request
     * @return updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request received");

        UserProfileResponse response = userService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * Change password for current user.
     *
     * Security: Requires current password verification
     *
     * @param request password change request
     * @return success message
     */
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request received");

        userService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null));
    }
}

