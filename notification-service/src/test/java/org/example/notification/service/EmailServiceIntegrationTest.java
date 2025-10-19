package org.example.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        reset(mailSender);
    }

    @Test
    void sendWelcomeEmail_ShouldSendCorrectMessage() {
        // Given
        String email = "test@example.com";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendWelcomeEmail(email);

        // Then
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals("Добро пожаловать!", sentMessage.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.", sentMessage.getText());
    }

    @Test
    void sendGoodbyeEmail_ShouldSendCorrectMessage() {
        // Given
        String email = "test@example.com";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendGoodbyeEmail(email);

        // Then
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals("Ваш аккаунт удален", sentMessage.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", sentMessage.getText());
    }

    @Test
    void sendEmail_ShouldSendCustomMessage() {
        // Given
        String email = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendEmail(email, subject, text);

        // Then
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(text, sentMessage.getText());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
