package com.example.circuitbreaker.core;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CircuitBreakerRegistry {
    private final ConcurrentHashMap<String, CircuitBreakerImpl> breakers = new ConcurrentHashMap<>();

    public CircuitBreakerImpl getCircuitBreaker(String name) {
        return breakers.computeIfAbsent(name,
            k -> new CircuitBreakerImpl(name, 5, 30000));
    }

    public CircuitBreakerImpl getCircuitBreaker(String name, int failureThreshold, long timeoutMs) {
        return breakers.computeIfAbsent(name,
            k -> new CircuitBreakerImpl(name, failureThreshold, timeoutMs));
    }

    public CircuitBreakerImpl.State getCircuitBreakerState(String name) {
        CircuitBreakerImpl breaker = breakers.get(name);
        return breaker != null ? breaker.getState() : CircuitBreakerImpl.State.CLOSED;
    }
}
