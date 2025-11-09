package com.example.ratelimiter.kafka;

import org.example.common.event.RateLimitResultEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimitResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RateLimitResultProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRateLimitResult(RateLimitResultEvent result) {
        kafkaTemplate.send("rate-limit-results", result.getRequestId(), result)
            .whenComplete((sendResult, exception) -> {
            });
    }
}
