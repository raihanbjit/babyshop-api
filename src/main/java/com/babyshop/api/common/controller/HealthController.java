package com.babyshop.api.common.controller;

import com.babyshop.api.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check and info endpoints.
 */
@RestController
@RequestMapping("/api/v1/public")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        health.put("service", "babyshop-api");
        health.put("version", "0.0.1-SNAPSHOT");

        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "Baby Shop E-commerce API");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("description", "Secure e-commerce backend for Baby Shop Bangladesh");

        return ResponseEntity.ok(ApiResponse.success(info));
    }
}

