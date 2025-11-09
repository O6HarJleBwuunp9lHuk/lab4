package com.example.ratelimiter.controller;

import com.example.ratelimiter.service.RateLimitService;
import org.example.common.event.RateLimitRequestEvent;
import org.example.common.event.RateLimitResultEvent;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/check")
    public RateLimitResultEvent checkRateLimit(@RequestBody RateLimitRequestEvent request) {
        return rateLimitService.checkRateLimit(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "rate-limiter-service");
    }
}
