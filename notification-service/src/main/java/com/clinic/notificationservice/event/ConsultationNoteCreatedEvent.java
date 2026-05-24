package com.clinic.notificationservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultationNoteCreatedEvent(
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
