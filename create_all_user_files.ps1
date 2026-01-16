# PowerShell script to create ALL User module files
# Run from: D:\baby-shop-ecommerce\babyshop-api

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# AuthService
$authServiceContent = @'
package com.babyshop.api.user.service;

import com.babyshop.api.common.exception.BusinessException;
import com.babyshop.api.security.JwtService;
import com.babyshop.api.user.dto.AuthResponse;
import com.babyshop.api.user.dto.LoginRequest;
import com.babyshop.api.user.dto.RegisterRequest;
import com.babyshop.api.user.dto.UserResponse;
import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.entity.UserRole;
import com.babyshop.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.email())) {
            throw new BusinessException(
                    "EMAIL_EXISTS",
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

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

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        String token = jwtService.generateToken(user);
        UserResponse userResponse = UserResponse.from(user);

        return AuthResponse.of(token, 86400000L, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase(),
                        request.password()
                )
        );

        User user = (User) authentication.getPrincipal();

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        UserResponse userResponse = UserResponse.from(user);

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.of(token, 86400000L, userResponse);
    }
}
'@

# UserService
$userServiceContent = @'
package com.babyshop.api.user.service;

import com.babyshop.api.common.exception.ResourceNotFoundException;
import com.babyshop.api.user.dto.UserResponse;
import com.babyshop.api.user.entity.User;
import com.babyshop.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile() {
        User user = getCurrentUser();
        return UserResponse.from(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .filter(user -> !user.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
}
'@

# Create files
[System.IO.File]::WriteAllText("src\main\java\com\babyshop\api\user\service\AuthService.java", $authServiceContent, $utf8NoBom)
Write-Host "Created: AuthService.java" -ForegroundColor Green

[System.IO.File]::WriteAllText("src\main\java\com\babyshop\api\user\service\UserService.java", $userServiceContent, $utf8NoBom)
Write-Host "Created: UserService.java" -ForegroundColor Green

Write-Host "`nAll User module files created successfully!" -ForegroundColor Cyan
Write-Host "Now run: .\gradlew clean build" -ForegroundColor Yellow

