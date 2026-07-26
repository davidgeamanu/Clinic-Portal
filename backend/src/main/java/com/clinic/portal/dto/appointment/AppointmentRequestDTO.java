package com.clinic.portal.dto.appointment;

import com.clinic.portal.model.enums.AppointmentMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(

        @NotNull(message = "Doctor is required")
        Long doctorId,

        @NotNull(message = "Scheduled time is required")
        @Future(message = "Appointment must be scheduled in the future")
        LocalDateTime scheduledAt,

        // The upper bound matters as much as the lower one: the booking exclusion
        // constraint reserves the whole range, so an unbounded duration would let
        // a single request block a doctor's calendar indefinitely.
        @Min(value = 15, message = "Minimum consultation duration is 15 minutes")
        @Max(value = 240, message = "Maximum consultation duration is 4 hours")
        int durationMinutes,

        AppointmentMode mode,

        String reason

) {}
