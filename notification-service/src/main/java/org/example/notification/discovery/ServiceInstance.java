package org.example.notification.discovery;

import java.util.Map;
import java.util.HashMap;

public class ServiceInstance {
    private String instanceId;
    private String serviceName;
    private String host;
    private int port;
    private String healthCheckUrl;
    private Map<String, String> metadata;
    private long lastHeartbeat;
    private int load;

    public ServiceInstance() {
        this.metadata = new HashMap<>();
    }

    public ServiceInstance(String instanceId, String serviceName, String host, int port) {
        this();
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
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

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    public boolean isHealthy() {
        return System.currentTimeMillis() - lastHeartbeat < 45000;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
            "instanceId='" + instanceId + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", host='" + host + '\'' +
            ", port=" + port +
            ", lastHeartbeat=" + lastHeartbeat +
            ", load=" + load +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return instanceId != null ? instanceId.equals(that.instanceId) : that.instanceId == null;
    }

    @Override
    public int hashCode() {
        return instanceId != null ? instanceId.hashCode() : 0;
    }
}
