package com.clinic.portal.model.enums;

/**
 * Lifecycle states of an Appointment.
 *
 *   Valid transitions:
 *   SCHEDULED → CONFIRMED → COMPLETED
 *   SCHEDULED → CANCELLED
 *   CONFIRMED → CANCELLED
 *   SCHEDULED → RESCHEDULED → CONFIRMED → COMPLETED
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    RESCHEDULED
}
