package com.clinic.portal.event;

import com.clinic.portal.model.enums.AppointmentStatus;

/**
 * Domain event published whenever an appointment transitions between statuses.
 */
public record AppointmentStatusChangedEvent(
        Long appointmentId,
        AppointmentStatus previousStatus,
        AppointmentStatus newStatus,
        Long actorUserId
) {
}