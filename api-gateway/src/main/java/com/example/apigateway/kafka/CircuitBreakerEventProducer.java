package com.example.apigateway.kafka;

import org.example.common.event.CircuitBreakerEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerEventProducer {

    private final KafkaTemplate<String, CircuitBreakerEvent> kafkaTemplate;

    public CircuitBreakerEventProducer(KafkaTemplate<String, CircuitBreakerEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void recordSuccess(String breakerName, String serviceName) {
        CircuitBreakerEvent event = new CircuitBreakerEvent();
        event.setBreakerName(breakerName);
        event.setEventType("SUCCESS_RECORDED");
        event.setServiceName(serviceName);
        event.setTimestamp(java.time.LocalDateTime.now());
        kafkaTemplate.send("circuit-breaker-events", breakerName, event);
    }

    public void recordFailure(String breakerName, String serviceName) {
        CircuitBreakerEvent event = new CircuitBreakerEvent();
        event.setBreakerName(breakerName);
        event.setEventType("FAILURE_RECORDED");
        event.setServiceName(serviceName);
        event.setTimestamp(java.time.LocalDateTime.now());
        kafkaTemplate.send("circuit-breaker-events", event);
    }
}
