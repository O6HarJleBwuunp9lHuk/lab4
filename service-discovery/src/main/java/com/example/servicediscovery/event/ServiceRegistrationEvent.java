package com.example.servicediscovery.event;

import java.time.LocalDateTime;

public class ServiceRegistrationEvent {
    private String instanceId;
    private String serviceName;
    private String host;
    private int port;
    private String healthCheckUrl;
    private LocalDateTime timestamp;

    public ServiceRegistrationEvent(String instanceId,
                                    String serviceName,
                                    String host,
                                    int port, String healthCheckUrl, LocalDateTime timestamp) {
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.healthCheckUrl = healthCheckUrl;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
