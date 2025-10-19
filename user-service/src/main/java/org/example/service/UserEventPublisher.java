package org.example.service;

import org.example.event.UserEvent;
import org.example.event.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(String email) {
        UserEvent event = new UserEvent(email, EventType.USER_CREATED);
        kafkaTemplate.send(userEventsTopic, email, event);
    }

    public void publishUserDeleted(String email) {
        UserEvent event = new UserEvent(email, EventType.USER_DELETED);
        kafkaTemplate.send(userEventsTopic, email, event);
    }
}
