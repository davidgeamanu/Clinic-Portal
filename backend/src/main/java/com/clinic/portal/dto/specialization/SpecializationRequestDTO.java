package com.clinic.portal.dto.specialization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecializationRequestDTO(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description

) {}
