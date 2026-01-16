# PowerShell script to create AuthController and RegisterRequest
# Run this from: D:\baby-shop-ecommerce\babyshop-api

$ErrorActionPreference = "Stop"

# AuthController content
$authControllerContent = @'
package com.babyshop.api.user.controller;

import com.babyshop.api.common.dto.ApiResponse;
import com.babyshop.api.user.dto.AuthResponse;
import com.babyshop.api.user.dto.LoginRequest;
import com.babyshop.api.user.dto.RegisterRequest;
import com.babyshop.api.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
'@

# RegisterRequest content
$registerRequestContent = @'
package com.babyshop.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
        )
        String password,

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
        String phoneNumber
) {
}
'@

# Create files with UTF-8 encoding (no BOM)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

$authControllerPath = "src\main\java\com\babyshop\api\user\controller\AuthController.java"
$registerRequestPath = "src\main\java\com\babyshop\api\user\dto\RegisterRequest.java"

[System.IO.File]::WriteAllText($authControllerPath, $authControllerContent, $utf8NoBom)
Write-Host "Created: $authControllerPath" -ForegroundColor Green

[System.IO.File]::WriteAllText($registerRequestPath, $registerRequestContent, $utf8NoBom)
Write-Host "Created: $registerRequestPath" -ForegroundColor Green

Write-Host "`nBoth files created successfully!" -ForegroundColor Cyan
Write-Host "Now run: .\gradlew clean build" -ForegroundColor Yellow

