package com.example.apigateway.kafka;

import org.example.common.event.RateLimitRequestEvent;
import org.example.common.event.RateLimitResultEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RateLimitProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RateLimitProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<RateLimitResultEvent> checkRateLimit(String clientId, String serviceName, String endpoint) {
        RateLimitRequestEvent request = new RateLimitRequestEvent();
        request.setClientId(clientId);
        request.setServiceName(serviceName);
        request.setEndpoint(endpoint);
        request.setLimit(100); // можно настраивать
        request.setWindowMs(60000);

        CompletableFuture<RateLimitResultEvent> future = new CompletableFuture<>();

        // Сохраняем future для обработки ответа
        RateLimitResponseCache.put(request.getRequestId(), future);

        kafkaTemplate.send("rate-limit-requests", request.getRequestId(), request)
            .whenComplete((result, exception) -> {
                if (exception != null) {
                    System.err.println("❌ Failed to send rate limit request: " + exception.getMessage());
                    // При ошибке Kafka разрешаем запрос
                    RateLimitResultEvent fallback = new RateLimitResultEvent();
                    fallback.setRequestId(request.getRequestId());
                    fallback.setAllowed(true);
                    fallback.setRemainingRequests(100);
                    future.complete(fallback);
                } else {
                    System.out.println("✅ Rate limit request sent: " + request.getRequestId());
                }
            });

        return future;
    }
}
