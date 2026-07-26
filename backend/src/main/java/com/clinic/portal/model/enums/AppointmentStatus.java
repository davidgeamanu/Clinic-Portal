package com.clinic.portal.model.enums;

/**
 * Lifecycle states of an Appointment.
 *
 *   Valid transitions:
 *   SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED
 *   SCHEDULED → CANCELLED
 *   CONFIRMED → CANCELLED
 *   CONFIRMED → NO_SHOW
 *   IN_PROGRESS → CANCELLED
 *
 *   NO_SHOW means a confirmed appointment the patient did not attend. It is set
 *   only by a doctor or admin, only after the scheduled start time, and never by
 *   the patient. Bookings still unconfirmed at their start time are auto-cancelled
 *   by AppointmentExpirationJob instead.
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
