package com.clinic.portal.dto.user;

import com.clinic.portal.model.enums.Role;

/**
 * Safe representation of a User — never includes passwordHash.
 * Used wherever user account info is displayed (admin panel, profile pages).
 */
public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        Role role,
        boolean active
) {}
