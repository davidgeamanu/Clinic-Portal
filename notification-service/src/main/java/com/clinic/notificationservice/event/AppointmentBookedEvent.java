package com.clinic.notificationservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentBookedEvent(
        UUID eventId,
        Long appointmentId,
        Long patientUserId,
        String patientName,
        String patientEmail,
        Long doctorUserId,
        String doctorName,
        String doctorEmail,
        LocalDateTime scheduledAt
) {}
