package com.clinic.portal.dto.consultation;

import jakarta.validation.constraints.NotBlank;

/**
 * Sent by the doctor when recording notes after completing an appointment.
 * Only diagnosis is required — other fields are optional clinical detail.
 */
public record ConsultationNoteRequestDTO(

        @NotBlank(message = "Diagnosis is required")
        String diagnosis,

        String treatment,
        String prescription,
        String notes

) {}
