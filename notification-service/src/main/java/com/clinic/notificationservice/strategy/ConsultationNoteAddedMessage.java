package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class ConsultationNoteAddedMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.CONSULTATION_NOTE_ADDED;
    }

    @Override
    public String build(NotificationContext context) {
        return "Dr. " + context.doctorName() + " has added consultation notes for your appointment on "
                + context.formattedDate();
    }
}
