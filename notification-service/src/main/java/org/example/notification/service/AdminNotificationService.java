package org.example.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AdminNotificationService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCriticalAlert(String message) {
        logger.error("CRITICAL ALERT: {}", message);
        try {
            kafkaTemplate.send("admin-alert-events", message);
        } catch (Exception e) {
            logger.error("Failed to send admin alert to Kafka: {}", e.getMessage());
        }
    }

    public void sendWarningAlert(String message) {
        logger.warn("WARNING: {}", message);

        try {
            kafkaTemplate.send("admin-alert-events", message);
        } catch (Exception e) {
            logger.error("Failed to send warning alert to Kafka: {}", e.getMessage());
        }
    }
}
