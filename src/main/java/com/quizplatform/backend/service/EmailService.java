package com.quizplatform.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String link) {
        send(to, "Reset your Quiz Platform password",
                "Hi " + name + ",\n\n"
                        + "We received a request to reset your password.\n"
                        + "Click the link below (valid for 1 hour):\n" + link + "\n\n"
                        + "If you did not request this, you can ignore this email.\n\n"
                        + "Quiz Platform Team");
    }

    @Async
    public void sendResultEmail(String to, String name, String quizTitle, String score) {
        send(to, "Your quiz result: " + quizTitle,
                "Hi " + name + ",\n\n"
                        + "You scored " + score + " on \"" + quizTitle + "\".\n\n"
                        + "Quiz Platform Team");
    }

    private void send(String to, String subject, String text) {
        try {
            if (from == null || from.isBlank()) {
                log.info("SMTP not configured, skipping email to {}", to);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}