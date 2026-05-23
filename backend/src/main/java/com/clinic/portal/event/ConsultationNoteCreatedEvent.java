package com.clinic.portal.event;

/**
 * Domain event published once a doctor records a consultation note for an
 * appointment. The patient is notified that their notes are available.
 */
public record ConsultationNoteCreatedEvent(Long noteId, Long appointmentId) {
}