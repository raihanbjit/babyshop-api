package com.babyshop.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * CORS configuration properties.
 * Externalized for better security and environment-specific configuration.
 */
@Component
@ConfigurationProperties(prefix = "security.cors")
@Validated
@Getter
@Setter
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Should be configured per environment (dev, staging, prod).
     */
    @NotEmpty(message = "At least one allowed origin must be configured")
    private List<String> allowedOrigins;

    /**
     * Maximum age (in seconds) for CORS preflight cache.
     * Default: 3600 (1 hour)
     */
    private Long maxAge = 3600L;
}

