package com.example.servicediscovery.service;

import com.example.servicediscovery.event.*;
import org.example.common.event.ServiceInstance;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServiceRegistryService {

    private final Map<String, ServiceInstance> services = new ConcurrentHashMap<>();
    private final Map<String, Long> heartbeats = new ConcurrentHashMap<>();

    public void registerService(ServiceRegistrationEvent event) {
        ServiceInstance instance = new ServiceInstance();
        instance.setInstanceId(event.getInstanceId());
        instance.setServiceName(event.getServiceName());
        instance.setHost(event.getHost());
        instance.setPort(event.getPort());
        instance.setHealthCheckUrl(event.getHealthCheckUrl());
        instance.setLastHeartbeat(System.currentTimeMillis());

        services.put(event.getInstanceId(), instance);
        heartbeats.put(event.getInstanceId(), System.currentTimeMillis());

        System.out.println("Service registered: " + event.getInstanceId());
    }

    public void updateHeartbeat(ServiceHeartbeatEvent event) {
        heartbeats.put(event.getInstanceId(), System.currentTimeMillis());
        ServiceInstance instance = services.get(event.getInstanceId());
        if (instance != null) {
            instance.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    public void unregisterService(ServiceUnregistrationEvent event) {
        services.remove(event.getInstanceId());
        heartbeats.remove(event.getInstanceId());
        System.out.println("Service unregistered: " + event.getInstanceId());
    }

    public List<ServiceInstance> getServices(String serviceName) {
        return services.values().stream()
            .filter(instance -> serviceName.equals(instance.getServiceName()))
            .filter(this::isServiceAlive)
            .toList();
    }

    public Optional<ServiceInstance> getService(String serviceName) {
        return getServices(serviceName).stream().findFirst();
    }

    private boolean isServiceAlive(ServiceInstance instance) {
        Long lastHeartbeat = heartbeats.get(instance.getInstanceId());
        return lastHeartbeat != null &&
            (System.currentTimeMillis() - lastHeartbeat) < 30000;
    }

    public void cleanupDeadServices() {
        Iterator<Map.Entry<String, Long>> iterator = heartbeats.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue() > 30000) {
                services.remove(entry.getKey());
                iterator.remove();
                System.out.println("Cleaned up dead service: " + entry.getKey());
            }
        }
    }
}
