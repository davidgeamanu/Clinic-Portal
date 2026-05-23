package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Message sent when an appointment is cancelled.
 */
@Component
public class AppointmentCancelledMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_CANCELLED;
    }

    @Override
    public String build(Appointment appointment) {
        return "Appointment on " + appointment.getScheduledAt() + " has been cancelled";
    }
}