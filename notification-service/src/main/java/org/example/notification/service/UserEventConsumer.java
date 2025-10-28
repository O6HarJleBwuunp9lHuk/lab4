package org.example.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.common.event.UserEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final EventProcessingService eventProcessingService;

    public UserEventConsumer(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @KafkaListener(topics = "user-events")
    public void consumeUserEvent(UserEvent event, Acknowledgment ack) {
        try {
            log.info("Received Kafka event: {}", event);

            eventProcessingService.processUserEvent(event);
            ack.acknowledge();

            log.info("Successfully processed event for: {}", event.getEmail());

        } catch (Exception e) {
            log.error("Failed to process event: {}", event, e);
        }
    }
}
