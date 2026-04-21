package com.clinic.portal.service.impl;

import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.specialization.SpecializationRequestDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        user.setActive(active);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateDataException(ExceptionCode.EMAIL_ALREADY_EXISTS);
        }
        if (doctorProfileRepository.existsByLicenseNumber(dto.licenseNumber())) {
            throw new DuplicateDataException(ExceptionCode.LICENSE_NUMBER_ALREADY_EXISTS);
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

        List<Specialization> specializations = new ArrayList<>();
        if (dto.specializationIds() != null && !dto.specializationIds().isEmpty()) {
            specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND)))
                    .collect(Collectors.toCollection(ArrayList::new));
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
    @Transactional
    public DoctorProfileResponseDTO updateDoctor(Long profileId, DoctorProfileUpdateDTO dto) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));

        if (dto.biography() != null)
            profile.setBiography(dto.biography());
        if (dto.consultationFee() != null)
            profile.setConsultationFee(dto.consultationFee());
        if (dto.specializationIds() != null) {
            List<Specialization> specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND)))
                    .collect(Collectors.toCollection(ArrayList::new));
            profile.setSpecializations(specializations);
        }

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
            throw new DuplicateDataException(ExceptionCode.SPECIALIZATION_ALREADY_EXISTS);
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
            throw new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND);
        }
        specializationRepository.deleteById(id);
    }
}