package org.example.notification.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegistration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistration.class);

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Value("${server.port:8081}")
    private int port;

    @Value("${spring.application.name:notification-service}")
    private String serviceName;

    private volatile boolean registered = false;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!registered) {
            registerService();
            startHeartbeat();
            registered = true;
            logger.info("Service registration completed for {} on port {}", serviceName, port);
        }
    }

    private void registerService() {
        ServiceInstance instance = new ServiceInstance();
        instance.setInstanceId(generateInstanceId());
        instance.setServiceName(serviceName);
        instance.setHost("localhost");
        instance.setPort(port);
        instance.setHealthCheckUrl("http://localhost:" + port + "/health");
        instance.setLoad(0);

        serviceRegistry.registerService(instance);
        logger.info("Service registered: {} at {}:{}", instance.getInstanceId(), instance.getHost(), instance.getPort());
    }

    private void startHeartbeat() {
        Thread heartbeatThread = new Thread(() -> {
            logger.debug("Starting heartbeat thread for service: {}", serviceName);
            while (!Thread.currentThread().isInterrupted() && registered) {
                try {
                    Thread.sleep(15000);

                    serviceRegistry.getServices(serviceName).stream()
                        .filter(instance -> instance.getHost().equals("localhost") && instance.getPort() == port)
                        .findFirst()
                        .ifPresent(instance -> {
                            instance.setLastHeartbeat(System.currentTimeMillis());
                            logger.debug("Heartbeat updated for service instance: {}", instance.getInstanceId());
                        });

                } catch (InterruptedException e) {
                    logger.info("Heartbeat thread interrupted, stopping...");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Heartbeat error for service: {}", serviceName, e);
                }
            }
            logger.info("Heartbeat thread stopped for service: {}", serviceName);
        }, "service-heartbeat-" + serviceName);

        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
        logger.debug("Heartbeat thread started for service: {}", serviceName);
    }

    private String generateInstanceId() {
        String instanceId = serviceName + "-" + System.currentTimeMillis() + "-" + Math.abs(new java.util.Random().nextInt(1000));
        logger.debug("Generated instance ID: {}", instanceId);
        return instanceId;
    }
}
