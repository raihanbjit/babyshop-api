package com.babyshop.api.security;

import com.babyshop.api.common.dto.ApiError;
import com.babyshop.api.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test cases for JwtAuthenticationEntryPoint.
 * Verifies proper error handling when authentication fails.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Entry Point Tests")
class JwtAuthenticationEntryPointTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = mock(AuthenticationException.class);
        when(authException.getMessage()).thenReturn("Authentication failed");

        // Configure ObjectMapper with JavaTimeModule
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should return 401 with proper error message when authentication fails")
    void shouldReturn401WhenAuthenticationFails() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/orders");
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(response.getContentAsString()).contains("Authentication failed");
        assertThat(response.getContentAsString()).contains("AUTHENTICATION_REQUIRED");
    }

    @Test
    @DisplayName("Should use custom error message from request attribute when present")
    void shouldUseCustomErrorMessageWhenPresent() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/profile");
        request.setAttribute("jwt_error", "Token has expired");
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Token has expired");
    }

    @Test
    @DisplayName("Should use default error message when no attribute is set")
    void shouldUseDefaultErrorMessageWhenNoAttributeSet() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/cart");
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Authentication required");
    }

    @Test
    @DisplayName("Should include request path in error response")
    void shouldIncludeRequestPathInErrorResponse() throws Exception {
        // Arrange
        String testPath = "/api/v1/admin/users";
        request.setRequestURI(testPath);
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains(testPath);
    }
}

