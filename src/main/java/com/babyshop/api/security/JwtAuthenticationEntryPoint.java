package com.babyshop.api.security;

import com.babyshop.api.common.dto.ApiError;
import com.babyshop.api.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point for JWT authentication failures.
 * Returns proper JSON error responses when authentication fails.
 *
 * Security hardening:
 * - Generic error messages for clients (no JWT validation details exposed)
 * - Detailed errors only in server logs
 * - No PII in responses or logs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // Log with request path only (no PII, no sensitive details)
        log.warn("Authentication failed for path: {} - Reason: {}",
                request.getRequestURI(),
                authException.getClass().getSimpleName());

        // Check for specific JWT error from filter (already generic)
        String errorMessage = (String) request.getAttribute("jwt_error");

        // Use generic message if none set
        if (errorMessage == null) {
            errorMessage = "Authentication required";
        }

        ApiError apiError = ApiError.builder()
                .code("AUTHENTICATION_REQUIRED")
                .details(errorMessage)
                .path(request.getRequestURI())
                .build();

        ApiResponse<Void> apiResponse = ApiResponse.error("Authentication failed", apiError);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

