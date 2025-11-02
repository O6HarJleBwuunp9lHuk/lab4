package org.example.notification.dto;

import java.time.LocalDateTime;

public class EmailRetryMessage {
    private String email;
    private String emailType;
    private LocalDateTime scheduledTime;
    private int retryCount;

    public EmailRetryMessage() {
    }

    public EmailRetryMessage(String email, String emailType, LocalDateTime scheduledTime, int retryCount) {
        this.email = email;
        this.emailType = emailType;
        this.scheduledTime = scheduledTime;
        this.retryCount = retryCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
