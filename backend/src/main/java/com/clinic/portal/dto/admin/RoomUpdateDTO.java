package com.clinic.portal.dto.admin;

import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;

public record RoomUpdateDTO(
        Long specializationId,
        RoomType type,
        RoomStatus status
) {}