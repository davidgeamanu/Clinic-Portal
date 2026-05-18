package com.clinic.portal.dto.doctor;

import java.time.LocalDateTime;

public record BookedSlotDTO(
        LocalDateTime scheduledAt,
        int durationMinutes
) {}