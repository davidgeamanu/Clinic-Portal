package com.clinic.portal.event;

/**
 * Domain event published after a patient successfully books an appointment.
 */
public record AppointmentBookedEvent(Long appointmentId) {
}