package com.clinic.portal.dto.consultation;

import jakarta.validation.constraints.NotBlank;

public record ConsultationNoteRequestDTO(

        @NotBlank(message = "Diagnosis is required")
        String diagnosis,

        String treatment,
        String prescription,
        String notes

) {}
