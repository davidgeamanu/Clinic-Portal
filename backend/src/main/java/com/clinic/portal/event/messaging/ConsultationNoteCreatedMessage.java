package com.clinic.portal.event.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fat event published to RabbitMQ when a doctor records a consultation note.
 * Notifies the patient that their notes are available.
 */
public record ConsultationNoteCreatedMessage(
        UUID eventId,
        Long noteId,
        Long appointmentId,
        Long patientUserId,
        String patientName,
        String patientEmail,
        Long doctorUserId,
        String doctorName,
        LocalDateTime scheduledAt
) {}
