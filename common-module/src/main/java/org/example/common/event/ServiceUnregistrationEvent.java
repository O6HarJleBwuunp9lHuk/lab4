package org.example.common.event;

import java.time.LocalDateTime;

public class ServiceUnregistrationEvent {
    private String instanceId;
    private String serviceName;
    private LocalDateTime timestamp;
    private String reason;

    public ServiceUnregistrationEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public ServiceUnregistrationEvent(String instanceId, String serviceName) {
        this();
        this.instanceId = instanceId;
        this.serviceName = serviceName;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ServiceUnregistrationEvent{" +
            "instanceId='" + instanceId + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", timestamp=" + timestamp +
            ", reason='" + reason + '\'' +
            '}';
    }
}
