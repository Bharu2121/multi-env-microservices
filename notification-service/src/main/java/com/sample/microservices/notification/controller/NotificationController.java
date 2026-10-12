package com.sample.microservices.notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notification-service"));
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notify(@RequestBody Map<String, Object> payload) {
        log.info("Received notification payload: {}", payload);
        return ResponseEntity.ok(Map.of(
                "status", "DELIVERED",
                "received", payload
        ));
    }
}
