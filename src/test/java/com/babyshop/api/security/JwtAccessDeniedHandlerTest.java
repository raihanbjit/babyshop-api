package com.babyshop.api.security;

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
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for JwtAccessDeniedHandler.
 * Verifies proper error handling when authorization fails.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Access Denied Handler Tests")
class JwtAccessDeniedHandlerTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AccessDeniedException accessDeniedException;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        accessDeniedException = new AccessDeniedException("Access is denied");

        // Configure ObjectMapper with JavaTimeModule
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should return 403 when access is denied")
    void shouldReturn403WhenAccessDenied() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/admin/users");
        JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(objectMapper);

        // Act
        handler.handle(request, response, accessDeniedException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(response.getContentAsString()).contains("Access denied");
    }

    @Test
    @DisplayName("Should include proper error code in response")
    void shouldIncludeProperErrorCode() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/admin/products");
        JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(objectMapper);

        // Act
        handler.handle(request, response, accessDeniedException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("ACCESS_DENIED");
    }

    @Test
    @DisplayName("Should include request path in error response")
    void shouldIncludeRequestPathInErrorResponse() throws Exception {
        // Arrange
        String testPath = "/api/v1/admin/settings";
        request.setRequestURI(testPath);
        JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(objectMapper);

        // Act
        handler.handle(request, response, accessDeniedException);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains(testPath);
    }

    @Test
    @DisplayName("Should return user-friendly error message")
    void shouldReturnUserFriendlyErrorMessage() throws Exception {
        // Arrange
        request.setRequestURI("/api/v1/admin/orders");
        JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(objectMapper);

        // Act
        handler.handle(request, response, accessDeniedException);

        // Assert
        assertThat(response.getContentAsString()).contains("don't have permission");
    }
}

