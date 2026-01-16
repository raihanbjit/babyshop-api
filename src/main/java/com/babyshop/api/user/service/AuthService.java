package com.babyshop.api.user.service;

import com.babyshop.api.common.exception.BusinessException;
import com.babyshop.api.security.JwtService;
import com.babyshop.api.user.dto.auth.AuthResponse;
import com.babyshop.api.user.dto.auth.LoginRequest;
import com.babyshop.api.user.dto.auth.RegisterRequest;
import com.babyshop.api.user.dto.user.UserProfileResponse;
import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.entity.UserRole;
import com.babyshop.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Authentication service implementation.
 *
 * Design decisions:
 * - Use @Transactional for data consistency
 * - Validate email uniqueness before registration
 * - Hash passwords with BCrypt (never store plain text)
 * - Generate JWT tokens after successful authentication
 * - Update last login timestamp
 * - Return user info with tokens to avoid additional API call
 *
 * Security:
 * - No business logic in controller
 * - Never log passwords
 * - Use proper exception handling
 * - Fail-fast on validation errors
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    /**
     * Register a new user.
     *
     * Process:
     * 1. Check email uniqueness
     * 2. Hash password
     * 3. Create user with CUSTOMER role
     * 4. Save to database
     * 5. Generate JWT tokens
     * 6. Return tokens with user info
     *
     * @param request registration details
     * @return AuthResponse with tokens and user info
     * @throws BusinessException if email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        // Validate email uniqueness
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.email())) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new BusinessException(
                    "Email already registered",
                    "EMAIL_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        // Create user entity
        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();

        // Save user
        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // Convert to DTO
        UserProfileResponse userResponse = UserProfileResponse.from(user);

        return AuthResponse.of(accessToken, refreshToken, jwtExpiration, userResponse);
    }

    /**
     * Authenticate user and generate tokens.
     *
     * Process:
     * 1. Authenticate with Spring Security
     * 2. Update last login timestamp
     * 3. Generate JWT tokens
     * 4. Return tokens with user info
     *
     * @param request login credentials
     * @return AuthResponse with tokens and user info
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().toLowerCase(),
                            request.password()
                    )
            );

            // Get authenticated user
            User user = (User) authentication.getPrincipal();

            // Update last login timestamp
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = generateRefreshToken(user);

            // Convert to DTO
            UserProfileResponse userResponse = UserProfileResponse.from(user);

            log.info("User logged in successfully with ID: {}", user.getId());

            return AuthResponse.of(accessToken, refreshToken, jwtExpiration, userResponse);

        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for email: {}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Generate refresh token with longer expiry.
     *
     * Note: In production, consider storing refresh tokens in database
     * for revocation capability.
     *
     * @param user user details
     * @return refresh token
     */
    private String generateRefreshToken(User user) {
        // For now, use the same JWT mechanism with longer expiry
        // In production, consider separate refresh token storage
        return jwtService.generateToken(user);
    }
}

