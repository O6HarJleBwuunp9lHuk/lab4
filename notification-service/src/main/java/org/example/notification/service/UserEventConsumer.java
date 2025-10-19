package org.example.notification.service;

import org.example.common.event.UserEvent;
import org.example.common.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);
    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-events")
    public void consumeUserEvent(UserEvent event, Acknowledgment ack) {
        try {
            logger.info("Received Kafka event: {}", event);

            if (event.getEmail() == null || event.getEventType() == null) {
                logger.warn("Invalid event received");
                ack.acknowledge();
                return;
            }

            if (event.getEventType() == EventType.USER_CREATED) {
                emailService.sendWelcomeEmail(event.getEmail());
                logger.info("Welcome email sent to: {}", event.getEmail());
            } else if (event.getEventType() == EventType.USER_DELETED) {
                emailService.sendGoodbyeEmail(event.getEmail());
                logger.info(" Goodbye email sent to: {}", event.getEmail());
            } else {
                logger.warn("Unknown event type: {}", event.getEventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process event: {}", event, e);
            ack.acknowledge();
        }
    }
}
