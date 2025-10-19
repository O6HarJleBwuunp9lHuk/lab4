package org.example.notification.service;

import org.example.notification.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-events}")
    public void consumeUserEvent(UserEvent event) {
        logger.info("Received user event: {} for email: {}", event.getEventType(), event.getEmail());

        try {
            switch (event.getEventType()) {
                case USER_CREATED:
                    emailService.sendWelcomeEmail(event.getEmail());
                    logger.info("Welcome email sent to: {}", event.getEmail());
                    break;
                case USER_DELETED:
                    emailService.sendGoodbyeEmail(event.getEmail());
                    logger.info("Goodbye email sent to: {}", event.getEmail());
                    break;
                default:
                    logger.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Failed to process user event for email: {}", event.getEmail(), e);
        }
    }
}
