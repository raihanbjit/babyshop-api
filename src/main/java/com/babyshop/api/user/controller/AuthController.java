package com.babyshop.api.user.controller;

import com.babyshop.api.common.dto.ApiResponse;
import com.babyshop.api.user.dto.auth.AuthResponse;
import com.babyshop.api.user.dto.auth.LoginRequest;
import com.babyshop.api.user.dto.auth.RegisterRequest;
import com.babyshop.api.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 *
 * Endpoints:
 * - POST /api/v1/auth/register - Public user registration
 * - POST /api/v1/auth/login - Public user login
 *
 * Design principles:
 * - No business logic in controller (delegate to service)
 * - Use @Valid for request validation
 * - Return consistent ApiResponse wrapper
 * - Proper HTTP status codes
 * - Log important events
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * @param request registration details
     * @return 201 Created with auth tokens and user info
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request received for email: {}", request.email());

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Authenticate user and generate tokens.
     *
     * @param request login credentials
     * @return 200 OK with auth tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email: {}", request.email());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }
}

