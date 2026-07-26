package com.clinic.portal.dto.triage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TriageRequestDTO(

        @NotBlank(message = "Please describe your symptoms")
        @Size(max = 2000, message = "Symptom description must be at most 2000 characters")
        String symptoms

) {}
