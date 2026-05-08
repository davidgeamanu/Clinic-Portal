package com.clinic.portal.dto.admin;

public record AdminDepartmentDTO(
        Long id,
        String name,
        String description,
        int doctorCount,
        int consultRoomCount,
        int orRoomCount,
        int imagingRoomCount
) {}