package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Message sent when an appointment transitions into the in-progress state.
 */
@Component
public class AppointmentStartedMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_STARTED;
    }

    @Override
    public String build(Appointment appointment) {
        return "Appointment on " + appointment.getScheduledAt() + " has been in_progress";
    }
}