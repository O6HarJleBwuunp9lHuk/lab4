package org.example.service;

import org.example.common.event.UserEvent;
import org.example.common.event.EventType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String userEventsTopic = "user-events";

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(String email) {
        UserEvent event = new UserEvent(email, EventType.USER_CREATED);
        kafkaTemplate.send(userEventsTopic, email, event);
        System.out.println("Event published: " + event);
    }

    public void publishUserDeleted(String email) {
        UserEvent event = new UserEvent(email, EventType.USER_DELETED);
        kafkaTemplate.send(userEventsTopic, email, event);
        System.out.println("Event published: " + event);
    }
}
