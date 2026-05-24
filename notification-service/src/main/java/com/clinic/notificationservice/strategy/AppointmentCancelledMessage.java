package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentCancelledMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_CANCELLED;
    }

    @Override
    public String build(NotificationContext context) {
        return "Your appointment on " + context.formattedDate()
                + " has been cancelled";
    }
}
