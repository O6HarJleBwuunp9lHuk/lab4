package org.example.notification.discovery;

public interface ServiceDiscoveryListener {
    void serviceRegistered(ServiceInstance instance);

    void serviceUnregistered(ServiceInstance instance);
}
