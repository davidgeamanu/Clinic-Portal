package com.clinic.portal.dto.appointment;

import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.RoomType;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        LocalDateTime scheduledAt,
        int durationMinutes,
        AppointmentMode mode,
        String roomNumber,
        RoomType roomType,
        String roomDepartment,
        AppointmentStatus status,
        String reason,
        LocalDateTime createdAt
) {}
