package com.clinic.portal.controller.auth;

import com.clinic.portal.controller.AuthenticatedController;
import com.clinic.portal.dto.auth.AuthResponseDTO;
import com.clinic.portal.dto.auth.ChangePasswordDTO;
import com.clinic.portal.dto.auth.LoginRequestDTO;
import com.clinic.portal.dto.auth.RegisterRequestDTO;
import com.clinic.portal.security.CookieService;
import com.clinic.portal.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController, AuthenticatedController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Override
    public void login(LoginRequestDTO dto) {
        // Handled by LoginAuthenticationFilter — this method exists only for Swagger documentation
    }

    @Override
    public void logout(HttpServletResponse response) {
        cookieService.clearAccessTokenCookie(response);
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        log.info("[AUTH] Registering new patient: {}", dto.email());
        return authService.register(dto);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void changePassword(ChangePasswordDTO dto) {
        log.info("[AUTH] Changing password for user: {}", currentUser().getId());
        authService.changePassword(currentUser().getId(), dto);
    }
}