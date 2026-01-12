package com.babyshop.api.security;

import com.babyshop.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT service for token generation and validation.
 *
 * Security enhancements:
 * - Validates issuer (iss) and audience (aud) claims
 * - Fail-fast on invalid claims
 * - Proper expiration handling
 * - No PII in logs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getExpiration());
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validates JWT token with comprehensive claim checks.
     *
     * Validates:
     * - Subject (username) matches
     * - Token not expired
     * - Issuer (iss) matches expected
     * - Audience (aud) matches expected
     *
     * Fail-fast approach: Any validation failure returns false immediately.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final Claims claims = extractAllClaims(token);
            final String username = claims.getSubject();

            // Validate subject (username)
            if (!username.equals(userDetails.getUsername())) {
                log.warn("JWT subject mismatch - token rejected");
                return false;
            }

            // Validate expiration
            if (isTokenExpired(token)) {
                log.warn("JWT token expired - token rejected");
                return false;
            }

            // Validate issuer claim
            String issuer = claims.getIssuer();
            if (issuer == null || !issuer.equals(jwtProperties.getIssuer())) {
                log.warn("JWT issuer claim invalid - token rejected");
                return false;
            }

            // Validate audience claim
            if (claims.getAudience() == null || !claims.getAudience().contains(jwtProperties.getAudience())) {
                log.warn("JWT audience claim invalid - token rejected");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from JWT token.
     *
     * Parser validates:
     * - Signature integrity
     * - Issuer claim (iss)
     * - Audience claim (aud)
     *
     * Throws exception if any validation fails (fail-fast).
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret())))
                .requireIssuer(jwtProperties.getIssuer())
                .requireAudience(jwtProperties.getAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

