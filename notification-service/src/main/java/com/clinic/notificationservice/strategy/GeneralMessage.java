package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class GeneralMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.GENERAL;
    }

    @Override
    public String build(NotificationContext context) {
        return "You have a new notification regarding your appointment on " + context.scheduledAt();
    }
}
