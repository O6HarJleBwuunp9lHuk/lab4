package org.example.common.event;

import java.time.LocalDateTime;

public class RateLimitResultEvent {
    private String requestId;
    private String clientId;
    private String serviceName;
    private String endpoint;
    private boolean allowed;
    private int remainingRequests;
    private int limit;
    private long resetTime;
    private LocalDateTime timestamp;

    public RateLimitResultEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public RateLimitResultEvent(String requestId, String clientId, boolean allowed) {
        this();
        this.requestId = requestId;
        this.clientId = clientId;
        this.allowed = allowed;
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

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RateLimitResultEvent{" +
            "requestId='" + requestId + '\'' +
            ", clientId='" + clientId + '\'' +
            ", allowed=" + allowed +
            ", remainingRequests=" + remainingRequests +
            ", limit=" + limit +
            ", resetTime=" + resetTime +
            ", timestamp=" + timestamp +
            '}';
    }
}
