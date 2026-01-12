package com.babyshop.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter.
 * Intercepts all requests and validates JWT tokens.
 *
 * Security hardening:
 * - Explicitly clears SecurityContext on token validation failure
 * - No PII (email, phone) in logs
 * - Never sets partial authentication
 * - Generic error messages for clients
 * - Detailed errors only in server logs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip filter for public endpoints
        final String requestPath = request.getServletPath();
        if (requestPath.startsWith("/api/v1/auth/") ||
            requestPath.startsWith("/api/v1/public/") ||
            requestPath.startsWith("/actuator/") ||
            requestPath.startsWith("/swagger-ui/") ||
            requestPath.startsWith("/v3/api-docs/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token provided - let SecurityContext remain empty
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            // If we have a token and no authentication is set yet
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate token comprehensively (issuer, audience, expiration, signature)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Log success without PII
                    log.debug("JWT authentication successful for request: {}", requestPath);
                } else {
                    // Token validation failed - explicitly clear context
                    SecurityContextHolder.clearContext();
                    log.warn("JWT validation failed for request: {}", requestPath);
                    // Generic error for client (no details exposed)
                    request.setAttribute("jwt_error", "Invalid authentication token");
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token expired - clear context and log without PII
            SecurityContextHolder.clearContext();
            log.warn("JWT expired for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            // Generic error for client
            request.setAttribute("jwt_error", "Authentication token expired");

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("Malformed JWT for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            request.setAttribute("jwt_error", "Invalid authentication token");

        } catch (io.jsonwebtoken.security.SignatureException e) {
            SecurityContextHolder.clearContext();
            log.warn("Invalid JWT signature for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            request.setAttribute("jwt_error", "Invalid authentication token");

        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("Unsupported JWT for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            request.setAttribute("jwt_error", "Invalid authentication token");

        } catch (IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.warn("Invalid JWT claims for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            request.setAttribute("jwt_error", "Invalid authentication token");

        } catch (Exception e) {
            // Catch-all for unexpected errors
            SecurityContextHolder.clearContext();
            log.error("Authentication error for request: {} - Exception: {}", requestPath, e.getClass().getSimpleName());
            request.setAttribute("jwt_error", "Authentication failed");
        }

        filterChain.doFilter(request, response);
    }
}

