package com.clinic.notificationservice.channel.email;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildSubject(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_SCHEDULED -> "New Appointment Booked";
            case APPOINTMENT_CONFIRMED -> "Appointment Confirmed";
            case APPOINTMENT_STARTED -> "Appointment Started";
            case APPOINTMENT_CANCELLED -> "Appointment Cancelled";
            case APPOINTMENT_NO_SHOW -> "Missed Appointment";
            case CONSULTATION_NOTE_ADDED -> "Consultation Notes Available";
            case GENERAL -> "Clinic Portal Notification";
        };
    }

    public String buildHtml(String recipientName, String message, NotificationType type) {
        String accentColor = switch (type) {
            case APPOINTMENT_SCHEDULED -> "#2563eb";
            case APPOINTMENT_CONFIRMED -> "#16a34a";
            case APPOINTMENT_STARTED -> "#7c3aed";
            case APPOINTMENT_CANCELLED -> "#dc2626";
            case APPOINTMENT_NO_SHOW -> "#d97706";
            case CONSULTATION_NOTE_ADDED -> "#0891b2";
            case GENERAL -> "#6b7280";
        };

        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f3f4f6;">
                  <div style="background: %s; color: white; padding: 24px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h2 style="margin: 0; font-size: 22px;">Clinic Portal</h2>
                    <p style="margin: 6px 0 0; font-size: 13px; opacity: 0.85;">%s</p>
                  </div>
                  <div style="padding: 28px 24px; background: white; border-left: 1px solid #e5e7eb; border-right: 1px solid #e5e7eb;">
                    <p style="font-size: 15px; color: #374151; margin: 0 0 16px;">Hello %s,</p>
                    <p style="font-size: 15px; color: #111827; margin: 0; line-height: 1.6;">%s</p>
                  </div>
                  <div style="padding: 16px; text-align: center; color: #9ca3af; font-size: 12px; border-radius: 0 0 8px 8px; background: #f9fafb; border: 1px solid #e5e7eb; border-top: none;">
                    This is an automated notification from Clinic Portal. Please do not reply.
                  </div>
                </body>
                </html>
                """.formatted(accentColor, buildSubject(type), recipientName, message);
    }
}
