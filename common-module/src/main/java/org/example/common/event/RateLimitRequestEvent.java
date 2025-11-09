package org.example.common.event;

import java.time.LocalDateTime;

public class RateLimitRequestEvent {
    private String requestId;
    private String clientId;
    private String serviceName;
    private String endpoint;
    private int limit = 100;
    private int windowMs = 60000;
    private LocalDateTime timestamp;

    public RateLimitRequestEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public RateLimitRequestEvent(String clientId, String serviceName, String endpoint) {
        this();
        this.requestId = generateRequestId();
        this.clientId = clientId;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getWindowMs() {
        return windowMs;
    }

    public void setWindowMs(int windowMs) {
        this.windowMs = windowMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private String generateRequestId() {
        return "rate-limit-" + System.currentTimeMillis() + "-" + Math.abs(hashCode());
    }

    @Override
    public String toString() {
        return "RateLimitRequestEvent{" +
            "requestId='" + requestId + '\'' +
            ", clientId='" + clientId + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", endpoint='" + endpoint + '\'' +
            ", limit=" + limit +
            ", windowMs=" + windowMs +
            ", timestamp=" + timestamp +
            '}';
    }
}
