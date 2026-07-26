package com.clinic.notificationservice.event;

/**
 * Mirrors the AppointmentStatus enum from the Clinic Portal service.
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
