package com.clinic.portal.event.messaging;

import com.clinic.portal.model.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fat event published to RabbitMQ on every appointment status transition.
 */
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
