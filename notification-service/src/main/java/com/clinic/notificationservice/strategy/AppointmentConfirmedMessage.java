package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentConfirmedMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_CONFIRMED;
    }

    @Override
    public String build(NotificationContext context) {
        return "Your appointment with Dr. " + context.doctorName()
                + " on " + context.formattedDate() + " has been confirmed";
    }
}
