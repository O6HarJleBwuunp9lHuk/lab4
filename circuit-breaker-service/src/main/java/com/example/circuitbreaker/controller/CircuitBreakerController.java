package com.example.circuitbreaker.controller;

import com.example.circuitbreaker.core.CircuitBreakerImpl;
import com.example.circuitbreaker.core.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/circuit-breaker")
public class CircuitBreakerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/{name}/allow")
    public Map<String, Object> allowRequest(@PathVariable String name) {
        CircuitBreakerImpl breaker = circuitBreakerRegistry.getCircuitBreaker(name);
        boolean allowed = breaker.allowRequest();

        return Map.of(
            "allowed", allowed,
            "state", breaker.getState().toString(),
            "breakerName", name
        );
    }

    @GetMapping("/{name}/status")
    public Map<String, Object> getStatus(@PathVariable String name) {
        CircuitBreakerImpl breaker = circuitBreakerRegistry.getCircuitBreaker(name);

        return Map.of(
            "state", breaker.getState().toString(),
            "breakerName", name
        );
    }
}
