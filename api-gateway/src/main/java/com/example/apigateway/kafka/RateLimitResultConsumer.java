package com.example.apigateway.kafka;

import org.example.common.event.RateLimitResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RateLimitResultConsumer {

    @KafkaListener(topics = "rate-limit-results")
    public void handleRateLimitResult(RateLimitResultEvent result) {
        System.out.println("📨 Received rate limit result: " + result.getRequestId() +
            " - Allowed: " + result.isAllowed() +
            " - Remaining: " + result.getRemainingRequests());

        RateLimitResponseCache.complete(result.getRequestId(), result);
    }
}
