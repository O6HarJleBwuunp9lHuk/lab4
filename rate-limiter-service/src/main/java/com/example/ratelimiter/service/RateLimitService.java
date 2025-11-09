package com.example.ratelimiter.service;

import org.example.common.event.RateLimitRequestEvent;
import org.example.common.event.RateLimitResultEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, ClientRateLimit> clients = new ConcurrentHashMap<>();

    @Value("${rate-limiter.default.limit:100}")
    private int defaultLimit;

    @Value("${rate-limiter.default.window-ms:60000}")
    private int defaultWindowMs;

    public RateLimitResultEvent checkRateLimit(RateLimitRequestEvent request) {
        String clientKey = generateClientKey(request.getClientId(), request.getServiceName());

        int limit = request.getLimit() > 0 ? request.getLimit() : defaultLimit;
        int windowMs = request.getWindowMs() > 0 ? request.getWindowMs() : defaultWindowMs;

        ClientRateLimit clientLimit = clients.computeIfAbsent(clientKey,
            k -> new ClientRateLimit(limit, windowMs));

        boolean allowed = clientLimit.allowRequest();
        int remaining = clientLimit.getRemainingRequests();
        long resetTime = clientLimit.getResetTime();

        RateLimitResultEvent result = new RateLimitResultEvent();
        result.setRequestId(request.getRequestId());
        result.setClientId(request.getClientId());
        result.setServiceName(request.getServiceName());
        result.setEndpoint(request.getEndpoint());
        result.setAllowed(allowed);
        result.setRemainingRequests(remaining);
        result.setLimit(limit);
        result.setResetTime(resetTime);

        System.out.println("🔒 Rate limit check: " + clientKey +
            " - Allowed: " + allowed +
            " - Remaining: " + remaining + "/" + limit);

        return result;
    }

    @Scheduled(fixedRate = 300000) // Каждые 5 минут
    public void cleanupOldClients() {
        long currentTime = System.currentTimeMillis();
        int initialSize = clients.size();

        clients.entrySet().removeIf(entry ->
            currentTime - entry.getValue().getLastAccessTime() > 3600000 // 1 час
        );

        int removed = initialSize - clients.size();
        if (removed > 0) {
            System.out.println("🧹 Cleaned up " + removed + " old rate limit clients");
        }
    }

    private String generateClientKey(String clientId, String serviceName) {
        return serviceName + ":" + clientId;
    }

    private static class ClientRateLimit {
        private final int limit;
        private final int windowMs;
        private final AtomicInteger requests = new AtomicInteger(0);
        private volatile long windowStart;
        private volatile long lastAccessTime;

        public ClientRateLimit(int limit, int windowMs) {
            this.limit = limit;
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
        }

        public boolean allowRequest() {
            lastAccessTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();

            synchronized (this) {
                if (currentTime - windowStart > windowMs) {
                    requests.set(0);
                    windowStart = currentTime;
                }

                return requests.incrementAndGet() <= limit;
            }
        }

        public int getRemainingRequests() {
            synchronized (this) {
                int current = requests.get();
                return Math.max(0, limit - current);
            }
        }

        public long getResetTime() {
            return windowStart + windowMs;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }
}
