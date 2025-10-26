package org.example.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {
    private String email;
    private EventType eventType;
    private LocalDateTime timestamp;

    public UserEvent() {
    }

    public UserEvent(String email, EventType eventType) {
        this.email = email;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
            "email='" + email + '\'' +
            ", eventType=" + eventType +
            ", timestamp=" + timestamp +
            '}';
    }
}
