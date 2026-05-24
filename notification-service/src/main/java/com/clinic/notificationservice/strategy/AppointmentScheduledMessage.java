package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentScheduledMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_SCHEDULED;
    }

    @Override
    public String build(NotificationContext context) {
        return "New appointment booked by " + context.patientName()
                + " on " + context.formattedDate();
    }
}
