package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Fallback message used when no domain-specific notification type applies.
 */
@Component
public class GeneralMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.GENERAL;
    }

    @Override
    public String build(Appointment appointment) {
        return "You have a new notification regarding your appointment on " + appointment.getScheduledAt();
    }
}