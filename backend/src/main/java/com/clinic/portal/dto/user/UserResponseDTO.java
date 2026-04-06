package com.clinic.portal.dto.user;

import com.clinic.portal.model.enums.Role;


public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        Role role,
        boolean active
) {}
