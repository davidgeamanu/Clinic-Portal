package com.clinic.portal.dto.appointment;

import com.clinic.portal.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateDTO(

        @NotNull(message = "Status is required")
        AppointmentStatus status

) {}
