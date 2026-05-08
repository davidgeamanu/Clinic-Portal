package com.clinic.portal.dto.admin;

import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;

public record RoomResponseDTO(
        Long id,
        String roomNumber,
        int floor,
        RoomType type,
        RoomStatus status,
        Long specializationId,
        String specializationName,
        Long assignedDoctorId,
        String assignedDoctorName
) {}