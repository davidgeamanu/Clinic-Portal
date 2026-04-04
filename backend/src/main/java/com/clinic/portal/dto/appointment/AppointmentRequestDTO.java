package com.clinic.portal.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(

        @NotNull(message = "Doctor is required")
        Long doctorId,

        @NotNull(message = "Scheduled time is required")
        @Future(message = "Appointment must be scheduled in the future")
        LocalDateTime scheduledAt,

        @Min(value = 15, message = "Minimum consultation duration is 15 minutes")
        int durationMinutes,

        String reason

) {}
