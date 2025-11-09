package org.example.common.event;

import java.time.LocalDateTime;

public class ServiceHeartbeatEvent {
    private String instanceId;
    private String serviceName;
    private LocalDateTime timestamp;
    private int load;

    public ServiceHeartbeatEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public ServiceHeartbeatEvent(String instanceId, String serviceName) {
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

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    @Override
    public String toString() {
        return "ServiceHeartbeatEvent{" +
            "instanceId='" + instanceId + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", timestamp=" + timestamp +
            ", load=" + load +
            '}';
    }
}
