package org.example.notification.ratelimit;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private final ConcurrentHashMap<String, ClientRateLimit> clients = new ConcurrentHashMap<>();
    private final int defaultLimit = 100; // запросов в минуту
    private final int defaultWindowMs = 60000; // 1 минута

    public boolean allowRequest(String clientId) {
        return allowRequest(clientId, defaultLimit, defaultWindowMs);
    }

    public boolean allowRequest(String clientId, int limit, int windowMs) {
        ClientRateLimit clientLimit = clients.computeIfAbsent(clientId,
            k -> new ClientRateLimit(limit, windowMs));

        return clientLimit.allowRequest();
    }

    private static class ClientRateLimit {
        private final int limit;
        private final int windowMs;
        private final AtomicInteger requests = new AtomicInteger(0);
        private volatile long windowStart;

        public ClientRateLimit(int limit, int windowMs) {
            this.limit = limit;
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
        }

        public boolean allowRequest() {
            long currentTime = System.currentTimeMillis();

            if (currentTime - windowStart > windowMs) {
                requests.set(0);
                windowStart = currentTime;
            }

            return requests.incrementAndGet() <= limit;
        }
    }
}
