package com.example.circuitbreaker.kafka;

import com.example.circuitbreaker.core.CircuitBreakerImpl;
import org.example.common.event.CircuitBreakerEvent;
import com.example.circuitbreaker.core.CircuitBreakerRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerEventListener {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerEventListener(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @KafkaListener(topics = "circuit-breaker-events")
    public void handleCircuitBreakerEvent(CircuitBreakerEvent event) {
        CircuitBreakerImpl breaker = circuitBreakerRegistry.getCircuitBreaker(event.getBreakerName());

        switch (event.getEventType()) {
            case "SUCCESS_RECORDED":
                breaker.recordSuccess();
                break;
            case "FAILURE_RECORDED":
                breaker.recordFailure();
                break;
            default:
                System.out.println("Unknown event type: " + event.getEventType());
        }
    }
}
