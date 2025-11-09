package org.example.notification.kafka;

import org.example.common.event.ServiceRegistrationEvent;
import org.example.common.event.ServiceHeartbeatEvent;
import org.example.common.event.ServiceUnregistrationEvent;
import org.example.notification.config.EmailConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegistrationProducer {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistrationProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ServiceRegistrationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void registerService(ServiceRegistrationEvent event) {
        kafkaTemplate.send("service-registration-events", event.getInstanceId(), event);
        logger.info("Sent registration event: " + event.getInstanceId());
    }

    public void sendHeartbeat(ServiceHeartbeatEvent event) {
        kafkaTemplate.send("service-heartbeat-events", event.getInstanceId(), event);
        logger.info("Sent heartbeat event: " + event.getInstanceId());
    }

    public void unregisterService(ServiceUnregistrationEvent event) {
        kafkaTemplate.send("service-unregistration-events", event.getInstanceId(), event);
        logger.info("Sent unregistration event: " + event.getInstanceId());
    }
}
