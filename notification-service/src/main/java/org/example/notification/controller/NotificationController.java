package org.example.notification.controller;

import org.example.notification.dto.EmailRequest;
import org.example.notification.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendEmail(request.getEmail(), request.getSubject(), request.getMessage());
        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeEmail(@RequestParam String email) {
        emailService.sendWelcomeEmail(email);
        return ResponseEntity.ok("Welcome email sent successfully");
    }

    @PostMapping("/goodbye")
    public ResponseEntity<String> sendGoodbyeEmail(@RequestParam String email) {
        emailService.sendGoodbyeEmail(email);
        return ResponseEntity.ok("Goodbye email sent successfully");
    }
}
