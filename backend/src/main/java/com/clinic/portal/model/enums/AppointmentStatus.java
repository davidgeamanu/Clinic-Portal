package com.clinic.portal.model.enums;

/**
 * Lifecycle states of an Appointment.
 *
 *   Valid transitions:
 *   SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED
 *   SCHEDULED → CANCELLED
 *   CONFIRMED → CANCELLED
 *   IN_PROGRESS → CANCELLED
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
