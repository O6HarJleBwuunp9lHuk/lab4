package org.example.notification.service;

import org.example.notification.dto.EmailRetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailRetryConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailRetryConsumer.class);
    private final EmailService emailService;

    public EmailRetryConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "email-retry-events")
    public void consumeEmailRetryEvent(EmailRetryMessage retryMessage, Acknowledgment ack) {
        try {
            logger.info("Processing email retry event for: {}, type: {}",
                retryMessage.getEmail(), retryMessage.getEmailType());

            if (retryMessage.getScheduledTime().isAfter(LocalDateTime.now())) {
                logger.info("Retry event for {} is not due yet, skipping", retryMessage.getEmail());
                ack.acknowledge();
                return;
            }

            emailService.retryEmailSending(retryMessage);
            ack.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process email retry event for: {}", retryMessage.getEmail(), e);
        }
    }
}
