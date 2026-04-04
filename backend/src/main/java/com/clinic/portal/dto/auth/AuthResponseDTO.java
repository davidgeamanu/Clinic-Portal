package com.clinic.portal.dto.auth;

import com.clinic.portal.model.enums.Role;

/**
 * Returned in the response body after a successful login or registration.
 * The JWT itself is not included here — it is set as an HttpOnly cookie by the server.
 * role and userId are included so the frontend can redirect to the correct dashboard
 * without an extra API call.
 */
public record AuthResponseDTO(
        Role role,
        Long userId
) {}
