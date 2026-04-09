package com.clinic.portal.service.impl;

import com.clinic.portal.dto.auth.AuthResponseDTO;
import com.clinic.portal.dto.auth.LoginRequestDTO;
import com.clinic.portal.dto.auth.RegisterRequestDTO;
import com.clinic.portal.exception.ConflictException;
import com.clinic.portal.exception.UnauthorizedException;
import com.clinic.portal.exception.ResourceNotFoundException;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.security.JwtUtil;
import com.clinic.portal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @RequiredArgsConstructor — Lombok generates a constructor for every `final` field.
 *   This is constructor injection without the boilerplate.
 *
 * @Transactional(readOnly = true) at class level — all methods default to read-only.
 *   Methods that write override this with @Transactional.
 *   Read-only transactions let Hibernate skip dirty-checking, improving performance.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .role(Role.PATIENT)
                .build();

        userRepository.save(user);

        // Create an empty profile immediately — the patient fills it in later
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .build();

        patientProfileRepository.save(profile);

        return new AuthResponseDTO(jwtUtil.generateToken(user), user.getRole(), user.getId());
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        // Use the same error message for both "email not found" and "wrong password".
        // Telling an attacker which one failed is an account enumeration vulnerability.
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been deactivated. Please contact the clinic.");
        }

        return new AuthResponseDTO(jwtUtil.generateToken(user), user.getRole(), user.getId());
    }
}
