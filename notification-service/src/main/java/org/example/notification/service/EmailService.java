package org.example.notification.service;

import org.example.notification.dto.EmailRetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AdminNotificationService adminNotificationService;
    private static final String EMAIL_RETRY_TOPIC = "email-retry-events";
    private static final String ADMIN_ALERTS_TOPIC = "admin-alert-events";

    public EmailService(JavaMailSender mailSender,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        AdminNotificationService adminNotificationService) {
        this.mailSender = mailSender;
        this.kafkaTemplate = kafkaTemplate;
        this.adminNotificationService = adminNotificationService;
    }

    @Transactional
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send email to: {}, error: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }


    public void sendWelcomeEmail(String email) {
        String subject = "Добро пожаловать!";
        String text = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
        sendEmail(email, subject, text);
    }

    public void sendWelcomeEmailFallback(String email) {
        logger.warn("Fallback: Could not send welcome email to: {}", email);
        scheduleWelcomeEmailRetry(email);
    }


    public void sendGoodbyeEmail(String email) {
        String subject = "Ваш аккаунт удален";
        String text = "Здравствуйте! Ваш аккаунт был удалён.";
        sendEmail(email, subject, text);
    }

    public void sendGoodbyeEmailFallback(String email) {
        logger.warn("Fallback: Could not send goodbye email to: {}", email);
        scheduleGoodbyeEmailRetry(email);
    }

    private void scheduleWelcomeEmailRetry(String email) {
        try {
            EmailRetryMessage retryMessage = new EmailRetryMessage(
                email,
                "WELCOME",
                LocalDateTime.now().plusMinutes(30),
                0
            );

            kafkaTemplate.send(EMAIL_RETRY_TOPIC, email, retryMessage);
            logger.info("Welcome email retry scheduled via Kafka for: {}", email);

        } catch (Exception e) {
            logger.error("Failed to schedule welcome email retry for: {}", email, e);
            adminNotificationService.sendWarningAlert(
                "Failed to schedule welcome email retry for: " + email
            );
        }
    }

    private void scheduleGoodbyeEmailRetry(String email) {
        try {
            EmailRetryMessage retryMessage = new EmailRetryMessage(
                email,
                "GOODBYE",
                LocalDateTime.now().plusMinutes(15), // Повтор через 15 минут (срочнее!)
                0
            );

            kafkaTemplate.send(EMAIL_RETRY_TOPIC, email, retryMessage);
            logger.info("Goodbye email retry scheduled via Kafka for: {}", email);
            adminNotificationService.sendCriticalAlert(
                "Goodbye email failed for: " + email + ". Scheduled retry via Kafka."
            );

        } catch (Exception e) {
            logger.error("Failed to schedule goodbye email retry for: {}", email, e);
            adminNotificationService.sendCriticalAlert(
                "CRITICAL: Failed to schedule goodbye email retry for: " + email + ". Error: " + e.getMessage()
            );
        }
    }

    public void retryEmailSending(EmailRetryMessage retryMessage) {
        try {
            logger.info("Attempting email retry for: {}, type: {}, attempt: {}",
                retryMessage.getEmail(), retryMessage.getEmailType(), retryMessage.getRetryCount());

            if ("WELCOME".equals(retryMessage.getEmailType())) {
                sendWelcomeEmail(retryMessage.getEmail());
            } else if ("GOODBYE".equals(retryMessage.getEmailType())) {
                sendGoodbyeEmail(retryMessage.getEmail());
            }

            logger.info("Email retry successful for: {}", retryMessage.getEmail());

        } catch (Exception e) {
            handleRetryFailure(retryMessage, e);
        }
    }

    private void handleRetryFailure(EmailRetryMessage retryMessage, Exception e) {
        retryMessage.setRetryCount(retryMessage.getRetryCount() + 1);

        if (retryMessage.getRetryCount() >= 3) {
            logger.error("Email retry limit exceeded for: {}, type: {}",
                retryMessage.getEmail(), retryMessage.getEmailType());

            if ("GOODBYE".equals(retryMessage.getEmailType())) {
                adminNotificationService.sendCriticalAlert(
                    "CRITICAL: Goodbye email failed after 3 retries for: " + retryMessage.getEmail()
                );
            }
        } else {
            retryMessage.setScheduledTime(calculateNextRetryTime(retryMessage.getRetryCount()));
            try {
                kafkaTemplate.send(EMAIL_RETRY_TOPIC, retryMessage.getEmail(), retryMessage);
                logger.info("Scheduled next retry attempt {} for: {}",
                    retryMessage.getRetryCount(), retryMessage.getEmail());
            } catch (Exception kafkaException) {
                logger.error("Failed to schedule next retry for: {}", retryMessage.getEmail(), kafkaException);
            }
        }
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        return switch (retryCount) {
            case 1 -> LocalDateTime.now().plusMinutes(30);
            case 2 -> LocalDateTime.now().plusHours(2);
            default -> LocalDateTime.now().plusHours(6);
        };
    }
}
