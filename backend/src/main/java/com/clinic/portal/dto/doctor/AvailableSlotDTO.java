package com.clinic.portal.dto.doctor;

import java.time.LocalDateTime;

/**
 * A bookable slot computed from the doctor's working hours minus existing
 * appointments.
 */
public record AvailableSlotDTO(
        LocalDateTime startTime,
        int durationMinutes
) {}
