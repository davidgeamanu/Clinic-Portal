package com.clinic.portal.service;

import com.clinic.portal.dto.auth.AuthResponseDTO;
import com.clinic.portal.dto.auth.ChangePasswordDTO;
import com.clinic.portal.dto.auth.RegisterRequestDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);
}
