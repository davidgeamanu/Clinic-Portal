package com.clinic.notificationservice.channel.email;

import com.clinic.notificationservice.model.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannel {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.email.enabled}")
    private boolean enabled;

    public void send(String recipientEmail, String recipientName, String message, NotificationType type) {
        if (!enabled) {
            log.debug("Email disabled — skipping email to {}", recipientEmail);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setFrom(fromAddress);
            helper.setSubject(templateService.buildSubject(type));
            helper.setText(templateService.buildHtml(recipientName, message, type), true);
            mailSender.send(mimeMessage);
            log.info("Email sent to {} for {}", recipientEmail, type);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} for {}: {}", recipientEmail, type, e.getMessage());
        }
    }
}
