package com.clinic.portal.event.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fat event published to RabbitMQ when a patient books an appointment.
 */
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
