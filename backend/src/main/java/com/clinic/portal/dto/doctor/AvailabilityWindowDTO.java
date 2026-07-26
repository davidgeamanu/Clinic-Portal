package com.clinic.portal.dto.doctor;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One recurring weekly working window (e.g. MONDAY 09:00–17:00).
 */
public record AvailabilityWindowDTO(

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime

) {}
