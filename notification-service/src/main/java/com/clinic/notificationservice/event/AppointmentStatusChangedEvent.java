package com.clinic.notificationservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentStatusChangedEvent(
        UUID eventId,
        Long appointmentId,
        AppointmentStatus previousStatus,
        AppointmentStatus newStatus,
        Long patientUserId,
        String patientName,
        String patientEmail,
        Long doctorUserId,
        String doctorName,
        String doctorEmail,
        Long requestingUserId,
        LocalDateTime scheduledAt
) {}
