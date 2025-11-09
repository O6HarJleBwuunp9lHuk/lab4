package com.example.servicediscovery.event;

import java.time.LocalDateTime;

public class ServiceUnregistrationEvent {
    private String instanceId;
    private String serviceName;
    private LocalDateTime timestamp;

    public ServiceUnregistrationEvent(String instanceId, String serviceName, LocalDateTime timestamp) {
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.timestamp = timestamp;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
