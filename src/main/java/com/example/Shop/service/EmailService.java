package com.example.Shop.service;

import com.example.Shop.entity.Order;
import com.example.Shop.entity.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(java.util.Optional<JavaMailSender> mailSender,
                        SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender.orElse(null);
        this.templateEngine = templateEngine;
    }

    public void sendPasswordResetEmail(String to, String name, String resetUrl) {
        if (mailSender == null) {
            log.warn("Password reset email not sent: JavaMailSender is not configured");
            return;
        }

        String html = templateEngine.process("email/reset-password", new Context(
                java.util.Locale.getDefault(),
                Map.of("name", name, "resetUrl", resetUrl)
        ));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Password Reset Request");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MailException | MessagingException e) {
            log.warn("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderStatusEmail(Order order, OrderStatus previousStatus) {
        if (mailSender == null) {
            log.warn("Email not sent: JavaMailSender is not configured (set spring.mail.host)");
            return;
        }

        String email = order.getUser().getEmail();
        String name = order.getUser().getFullName();
        if (name == null || name.isBlank()) {
            name = email;
        }

        String html = templateEngine.process("email/order-status", new Context(
                java.util.Locale.getDefault(),
                Map.of(
                        "name", name,
                        "orderId", order.getId(),
                        "previousStatus", previousStatus,
                        "currentStatus", order.getStatus(),
                        "total", "$" + String.format("%.2f", order.getTotalAmount())
                )
        ));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Order #" + order.getId() + " status updated to " + order.getStatus());
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status email sent to {} for order #{}", email, order.getId());
        } catch (MailException | MessagingException e) {
            log.warn("Failed to send status email to {} for order #{}: {}", email, order.getId(), e.getMessage());
        }
    }
}
