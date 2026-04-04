package com.clinic.portal.dto.appointment;

import com.clinic.portal.model.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        LocalDateTime scheduledAt,
        int durationMinutes,
        AppointmentStatus status,
        String reason,
        LocalDateTime createdAt
) {}
