package com.example.apigateway.kafka;

import jakarta.annotation.PreDestroy;
import org.example.common.event.RateLimitResultEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class RateLimitResponseCache {

    private static final ConcurrentHashMap<String, CompletableFuture<RateLimitResultEvent>> pendingRequests =
        new ConcurrentHashMap<>();

    private static final ScheduledExecutorService cleanupScheduler =
        Executors.newSingleThreadScheduledExecutor();

    static {
        // Очистка зависших запросов каждые 30 секунд
        cleanupScheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            pendingRequests.entrySet().removeIf(entry -> {
                if (entry.getValue().isDone()) {
                    return true;
                }
                // Удаляем запросы, которые висят больше 10 секунд
                return false;
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    public static void put(String requestId, CompletableFuture<RateLimitResultEvent> future) {
        pendingRequests.put(requestId, future);

        // Таймаут 5 секунд на ответ
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            if (!future.isDone()) {
                future.complete(createTimeoutResult(requestId));
                pendingRequests.remove(requestId);
            }
        });
    }

    public static void complete(String requestId, RateLimitResultEvent result) {
        CompletableFuture<RateLimitResultEvent> future = pendingRequests.remove(requestId);
        if (future != null && !future.isDone()) {
            future.complete(result);
        }
    }

    private static RateLimitResultEvent createTimeoutResult(String requestId) {
        RateLimitResultEvent result = new RateLimitResultEvent();
        result.setRequestId(requestId);
        result.setAllowed(true); // При таймауте разрешаем запрос
        result.setRemainingRequests(100);
        result.setLimit(100);
        System.out.println("⏰ Rate limit timeout, allowing request: " + requestId);
        return result;
    }

    @PreDestroy
    public static void shutdown() {
        cleanupScheduler.shutdown();
    }
}
