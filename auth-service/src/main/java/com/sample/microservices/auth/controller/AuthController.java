package com.sample.microservices.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "auth-service"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.getOrDefault("username", "guest");
        // Dummy token generation for sample microservice
        String dummyToken = "dummy-jwt-token-for-" + username + "-xyz123";
        return ResponseEntity.ok(Map.of(
                "token", dummyToken,
                "tokenType", "Bearer",
                "username", username,
                "expiresIn", 3600
        ));
    }
}
