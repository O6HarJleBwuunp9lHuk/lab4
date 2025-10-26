package org.example.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.common.event.UserEvent;
import org.example.common.event.EventType;
import org.springframework.stereotype.Service;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);
    private final EmailService emailService;

    public EventProcessingService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void processUserEvent(UserEvent event) {
        log.info("Processing user event: {}", event.getEventType());

        if (event.getEmail() == null || event.getEventType() == null) {
            log.warn("Invalid event received: missing email or eventType");
            return;
        }

        switch (event.getEventType()) {
            case USER_CREATED:
                emailService.sendWelcomeEmail(event.getEmail());
                log.info("Welcome email sent to: {}", event.getEmail());
                break;

            case USER_DELETED:
                emailService.sendGoodbyeEmail(event.getEmail());
                log.info("Goodbye email sent to: {}", event.getEmail());
                break;

            default:
                log.warn("Unknown event type: {}", event.getEventType());
                break;
        }
    }
}
