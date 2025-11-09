package org.example.common.event;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class ServiceRegistrationEvent {
    private String instanceId;
    private String serviceName;
    private String host;
    private int port;
    private String healthCheckUrl;
    private Map<String, String> metadata = new HashMap<>();
    private LocalDateTime timestamp;

    public ServiceRegistrationEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public ServiceRegistrationEvent(String instanceId, String serviceName, String host, int port) {
        this();
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.healthCheckUrl = "http://" + host + ":" + port + "/health";
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    @Override
    public String toString() {
        return "ServiceRegistrationEvent{" +
            "instanceId='" + instanceId + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", host='" + host + '\'' +
            ", port=" + port +
            ", timestamp=" + timestamp +
            '}';
    }
}
