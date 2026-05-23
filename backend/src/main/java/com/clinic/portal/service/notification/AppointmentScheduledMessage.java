package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Message sent to a doctor when a patient books a new appointment.
 */
@Component
public class AppointmentScheduledMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_SCHEDULED;
    }

    @Override
    public String build(Appointment appointment) {
        User patient = appointment.getPatient().getUser();
        return "New appointment booked by " + patient.getFirstName() + " " + patient.getLastName()
                + " on " + appointment.getScheduledAt();
    }
}