package org.example.notification.service;

import org.example.common.event.UserEvent;
import org.example.common.event.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
@Testcontainers
@SpringBootTest
class UserEventConsumerIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "org.example.common.event");
        registry.add("spring.kafka.producer.key-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers", () -> "false");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private EmailService emailService;

    @Test
    void whenUserCreatedEvent_thenSendWelcomeEmail() throws Exception {
        // Given
        String email = "welcome@example.com";
        UserEvent userEvent = new UserEvent(email, EventType.USER_CREATED);

        // When
        var sendResult = kafkaTemplate.send("user-events", email, userEvent)
            .get(10, TimeUnit.SECONDS);

        // Then
        assertNotNull(sendResult.getRecordMetadata(), "Сообщение должно быть доставлено в Kafka");
        assertEquals("user-events", sendResult.getRecordMetadata().topic(),
            "Сообщение должно быть в правильном топике");

        verify(emailService, timeout(15000).times(1))
            .sendWelcomeEmail(email);
    }

    @Test
    void whenUserDeletedEvent_thenSendGoodbyeEmail() throws Exception {
        // Given
        String email = "goodbye@example.com";
        UserEvent userEvent = new UserEvent(email, EventType.USER_DELETED);

        // When
        var sendResult = kafkaTemplate.send("user-events", email, userEvent)
            .get(10, TimeUnit.SECONDS);

        // Then
        assertNotNull(sendResult.getRecordMetadata(), "Сообщение должно быть доставлено в Kafka");

        verify(emailService, timeout(15000).times(1))
            .sendGoodbyeEmail(email);
    }

    @Test
    void whenEventWithoutEmail_thenNoEmailSent() throws Exception {
        // Given
        UserEvent invalidEvent = new UserEvent();
        invalidEvent.setEventType(EventType.USER_CREATED);

        // When
        var sendResult = kafkaTemplate.send("user-events", "invalid-key", invalidEvent)
            .get(10, TimeUnit.SECONDS);

        // Then
        assertNotNull(sendResult.getRecordMetadata(), "Сообщение должно быть доставлено в Kafka");

        Thread.sleep(5000);
        verify(emailService, never()).sendWelcomeEmail(anyString());
        verify(emailService, never()).sendGoodbyeEmail(anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
