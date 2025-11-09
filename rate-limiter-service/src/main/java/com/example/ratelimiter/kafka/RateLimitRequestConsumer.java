package com.example.ratelimiter.kafka;

import com.example.ratelimiter.service.RateLimitService;
import org.example.common.event.RateLimitRequestEvent;
import org.example.common.event.RateLimitResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RateLimitRequestConsumer {

    private final RateLimitService rateLimitService;
    private final RateLimitResultProducer resultProducer;

    public RateLimitRequestConsumer(RateLimitService rateLimitService,
                                    RateLimitResultProducer resultProducer) {
        this.rateLimitService = rateLimitService;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "rate-limit-requests")
    public void handleRateLimitRequest(RateLimitRequestEvent request) {

        try {
            RateLimitResultEvent result = rateLimitService.checkRateLimit(request);
            resultProducer.sendRateLimitResult(result);
        } catch (Exception e) {
            RateLimitResultEvent fallbackResult = new RateLimitResultEvent();
            fallbackResult.setRequestId(request.getRequestId());
            fallbackResult.setClientId(request.getClientId());
            fallbackResult.setAllowed(true);
            fallbackResult.setRemainingRequests(request.getLimit());
            fallbackResult.setLimit(request.getLimit());
            resultProducer.sendRateLimitResult(fallbackResult);
        }
    }
}
