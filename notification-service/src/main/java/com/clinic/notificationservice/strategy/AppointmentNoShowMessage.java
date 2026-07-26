package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentNoShowMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.APPOINTMENT_NO_SHOW;
    }

    @Override
    public String build(NotificationContext context) {
        return "You did not attend your appointment with " + context.doctorName()
                + " on " + context.formattedDate()
                + ", so it was recorded as a missed appointment. Please book a new appointment if you still need a consultation";
    }
}
