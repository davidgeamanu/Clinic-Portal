package com.clinic.portal.service.impl;

import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationRequestDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.exception.ConflictException;
import com.clinic.portal.exception.ResourceNotFoundException;
import com.clinic.portal.mapper.DoctorProfileMapper;
import com.clinic.portal.mapper.SpecializationMapper;
import com.clinic.portal.mapper.UserMapper;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final SpecializationMapper specializationMapper;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserResponseDTO setActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (doctorProfileRepository.existsByLicenseNumber(dto.licenseNumber())) {
            throw new ConflictException("A doctor with this license number already exists");
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .role(Role.DOCTOR)
                .build();

        userRepository.save(user);

        List<Specialization> specializations = List.of();
        if (dto.specializationIds() != null && !dto.specializationIds().isEmpty()) {
            specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Specialization not found: " + id)))
                    .toList();
        }

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .licenseNumber(dto.licenseNumber())
                .biography(dto.biography())
                .specializations(specializations)
                .build();

        return doctorProfileMapper.toDto(doctorProfileRepository.save(profile));
    }

    @Override
    public List<SpecializationResponseDTO> getAllSpecializations() {
        return specializationRepository.findAll().stream().map(specializationMapper::toDto).toList();
    }

    @Override
    @Transactional
    public SpecializationResponseDTO createSpecialization(SpecializationRequestDTO dto) {
        if (specializationRepository.existsByName(dto.name())) {
            throw new ConflictException("Specialization already exists: " + dto.name());
        }
        Specialization specialization = Specialization.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
        return specializationMapper.toDto(specializationRepository.save(specialization));
    }

    @Override
    @Transactional
    public void deleteSpecialization(Long id) {
        if (!specializationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Specialization not found");
        }
        specializationRepository.deleteById(id);
    }
}