package com.clinic.portal.service.impl;

import com.clinic.portal.dto.auth.AuthResponseDTO;
import com.clinic.portal.dto.auth.ChangePasswordDTO;
import com.clinic.portal.dto.auth.RegisterRequestDTO;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration only. Login is handled entirely by Spring Security
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateDataException(ExceptionCode.EMAIL_ALREADY_EXISTS);
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

        // Create an empty profile immediately, the patient fills it in later
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .build();

        patientProfileRepository.save(profile);

        return new AuthResponseDTO(user.getRole(), user.getId(), user.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ExceptionCode.INCORRECT_PASSWORD);
        }

        if (passwordEncoder.matches(dto.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ExceptionCode.PASSWORD_SAME_AS_CURRENT);
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }
}