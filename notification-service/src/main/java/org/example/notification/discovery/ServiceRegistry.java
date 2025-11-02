package org.example.notification.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceRegistry implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private final Map<String, ServiceInstance> services = new ConcurrentHashMap<>();
    private final List<ServiceDiscoveryListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public ServiceRegistry() {
        startCleanupTask();
        logger.info("ServiceRegistry initialized");
    }

    public void registerService(ServiceInstance instance) {
        instance.setLastHeartbeat(System.currentTimeMillis());
        services.put(instance.getInstanceId(), instance);
        notifyServiceRegistered(instance);
        logger.info("Service registered: {}", instance.getInstanceId());
    }

    public void unregisterService(String instanceId) {
        ServiceInstance instance = services.remove(instanceId);
        if (instance != null) {
            notifyServiceUnregistered(instance);
            logger.info("Service unregistered: {}", instanceId);
        } else {
            logger.warn("Attempted to unregister non-existent service: {}", instanceId);
        }
    }

    public void updateHeartbeat(String instanceId) {
        ServiceInstance instance = services.get(instanceId);
        if (instance != null) {
            instance.setLastHeartbeat(System.currentTimeMillis());
            logger.debug("Heartbeat updated for service: {}", instanceId);
        } else {
            logger.warn("Attempted to update heartbeat for non-existent service: {}", instanceId);
        }
    }

    public List<ServiceInstance> getServices(String serviceName) {
        List<ServiceInstance> result = services.values().stream()
            .filter(instance -> serviceName.equals(instance.getServiceName()))
            .filter(this::isServiceAlive)
            .sorted(Comparator.comparing(ServiceInstance::getLoad))
            .toList();

        logger.debug("Found {} alive instances for service: {}", result.size(), serviceName);
        return result;
    }

    public Optional<ServiceInstance> getService(String serviceName) {
        List<ServiceInstance> instances = getServices(serviceName);
        if (instances.isEmpty()) {
            logger.debug("No alive instances found for service: {}", serviceName);
            return Optional.empty();
        }

        ServiceInstance selected = instances.get(0);
        logger.debug("Selected service instance: {} for service: {}", selected.getInstanceId(), serviceName);
        return Optional.of(selected);
    }

    public List<ServiceInstance> getAllServices() {
        List<ServiceInstance> allServices = new ArrayList<>(services.values());
        logger.debug("Retrieved all {} registered services", allServices.size());
        return allServices;
    }

    public List<ServiceInstance> getAliveServices() {
        List<ServiceInstance> aliveServices = services.values().stream()
            .filter(this::isServiceAlive)
            .toList();

        logger.debug("Found {} alive services out of {}", aliveServices.size(), services.size());
        return aliveServices;
    }

    private boolean isServiceAlive(ServiceInstance instance) {
        boolean alive = System.currentTimeMillis() - instance.getLastHeartbeat() < 30000; // 30 сек timeout
        if (!alive) {
            logger.debug("Service {} is considered dead - last heartbeat: {}",
                instance.getInstanceId(), instance.getLastHeartbeat());
        }
        return alive;
    }

    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupDeadServices();
            } catch (Exception e) {
                logger.error("Error in service cleanup task", e);
            }
        }, 60, 30, TimeUnit.SECONDS);
        logger.debug("Service cleanup task scheduled");
    }

    private void cleanupDeadServices() {
        Iterator<Map.Entry<String, ServiceInstance>> iterator = services.entrySet().iterator();
        int cleanedCount = 0;

        while (iterator.hasNext()) {
            Map.Entry<String, ServiceInstance> entry = iterator.next();
            if (!isServiceAlive(entry.getValue())) {
                ServiceInstance deadInstance = entry.getValue();
                iterator.remove();
                notifyServiceUnregistered(deadInstance);
                cleanedCount++;
                logger.info("Cleaned up dead service: {}", deadInstance.getInstanceId());
            }
        }

        if (cleanedCount > 0) {
            logger.info("Service cleanup completed. Removed {} dead services", cleanedCount);
        } else {
            logger.debug("Service cleanup completed - no dead services found");
        }
    }

    private void notifyServiceRegistered(ServiceInstance instance) {
        logger.debug("Notifying {} listeners about service registration: {}",
            listeners.size(), instance.getInstanceId());

        for (ServiceDiscoveryListener listener : listeners) {
            try {
                listener.serviceRegistered(instance);
                logger.debug("Successfully notified listener: {} about service registration",
                    listener.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Error notifying listener for service registration: {}",
                    listener.getClass().getSimpleName(), e);
            }
        }
    }

    private void notifyServiceUnregistered(ServiceInstance instance) {
        logger.debug("Notifying {} listeners about service unregistration: {}",
            listeners.size(), instance.getInstanceId());

        for (ServiceDiscoveryListener listener : listeners) {
            try {
                listener.serviceUnregistered(instance);
                logger.debug("Successfully notified listener: {} about service unregistration",
                    listener.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Error notifying listener for service unregistration: {}",
                    listener.getClass().getSimpleName(), e);
            }
        }
    }

    public void addListener(ServiceDiscoveryListener listener) {
        listeners.add(listener);
        logger.info("Listener added to ServiceRegistry: {}", listener.getClass().getSimpleName());
    }

    public void removeListener(ServiceDiscoveryListener listener) {
        boolean removed = listeners.remove(listener);
        if (removed) {
            logger.info("Listener removed from ServiceRegistry: {}", listener.getClass().getSimpleName());
        } else {
            logger.warn("Attempted to remove non-existent listener: {}", listener.getClass().getSimpleName());
        }
    }

    public int getRegisteredServicesCount() {
        int count = services.size();
        logger.debug("Registered services count: {}", count);
        return count;
    }

    public int getAliveServicesCount() {
        int count = (int) services.values().stream()
            .filter(this::isServiceAlive)
            .count();
        logger.debug("Alive services count: {}", count);
        return count;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Shutting down ServiceRegistry...");

        cleanupScheduler.shutdown();

        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Cleanup scheduler did not terminate gracefully, forcing shutdown");
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("ServiceRegistry shutdown interrupted", e);
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        services.clear();
        listeners.clear();
        logger.info("ServiceRegistry shutdown complete");
    }
}
