package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Message sent when an appointment is confirmed by the doctor.
 */
@Component
public class AppointmentConfirmedMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_CONFIRMED;
    }

    @Override
    public String build(Appointment appointment) {
        return "Appointment on " + appointment.getScheduledAt() + " has been confirmed";
    }
}