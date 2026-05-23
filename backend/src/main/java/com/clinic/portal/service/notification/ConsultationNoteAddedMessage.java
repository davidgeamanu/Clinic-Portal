package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Message sent to a patient once the doctor records their consultation note.
 */
@Component
public class ConsultationNoteAddedMessage implements NotificationMessageStrategy {

    @Override
    public NotificationType type() {
        return NotificationType.CONSULTATION_NOTE_ADDED;
    }

    @Override
    public String build(Appointment appointment) {
        return "Your consultation notes are available for appointment on " + appointment.getScheduledAt();
    }
}