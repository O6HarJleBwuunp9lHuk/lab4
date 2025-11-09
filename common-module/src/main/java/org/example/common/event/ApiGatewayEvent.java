package org.example.common.event;

import java.time.LocalDateTime;

public class ApiGatewayEvent {
    private String serviceName;
    private String eventType;
    private String path;
    private String method;
    private LocalDateTime timestamp;

    public ApiGatewayEvent() {
    }

    public ApiGatewayEvent(String serviceName, String eventType, String path, String method) {
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.path = path;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
