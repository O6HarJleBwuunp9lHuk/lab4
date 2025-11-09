package org.example.common.event;

import java.time.LocalDateTime;

public class CircuitBreakerEvent {
    private String breakerName;
    private String eventType;
    private String serviceName;
    private String path;
    private String method;
    private LocalDateTime timestamp;

    public CircuitBreakerEvent() {
    }

    public CircuitBreakerEvent(String breakerName, String eventType, String serviceName) {
        this.breakerName = breakerName;
        this.eventType = eventType;
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
    }

    public String getBreakerName() {
        return breakerName;
    }

    public void setBreakerName(String breakerName) {
        this.breakerName = breakerName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
