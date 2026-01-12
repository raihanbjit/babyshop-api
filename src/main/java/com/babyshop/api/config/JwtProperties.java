package com.babyshop.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JWT configuration properties.
 * Externalized for better security and validation.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT secret key (should be strong and stored securely).
     */
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    /**
     * JWT token expiration time in milliseconds.
     */
    @NotNull(message = "JWT expiration must not be null")
    private Long expiration;

    /**
     * JWT issuer claim (iss).
     * Should be your application's identifier.
     */
    @NotBlank(message = "JWT issuer must not be blank")
    private String issuer;

    /**
     * JWT audience claim (aud).
     * Should be your application's intended audience.
     */
    @NotBlank(message = "JWT audience must not be blank")
    private String audience;
}

