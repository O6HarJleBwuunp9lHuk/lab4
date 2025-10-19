package org.example.event;

import java.time.LocalDateTime;

public class UserEvent {
    private String email;
    private EventType eventType;
    private LocalDateTime timestamp;

    public UserEvent() {}

    public UserEvent(String email, EventType eventType) {
        this.email = email;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
