package com.clinic.portal.service.impl;

import com.clinic.portal.dto.patient.PatientProfileResponseDTO;
import com.clinic.portal.dto.patient.PatientProfileUpdateDTO;
import com.clinic.portal.exception.ResourceNotFoundException;
import com.clinic.portal.mapper.PatientProfileMapper;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    public PatientProfileResponseDTO getProfile(Long userId) {
        PatientProfile profile = findProfileByUserId(userId);
        return patientProfileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public PatientProfileResponseDTO updateProfile(Long userId, PatientProfileUpdateDTO dto) {
        PatientProfile profile = findProfileByUserId(userId);

        // Null check on each field = patch semantics: only update what was provided.
        // Sending null for a field means "leave it as is", not "clear it".
        if (dto.dateOfBirth()            != null) profile.setDateOfBirth(dto.dateOfBirth());
        if (dto.gender()                 != null) profile.setGender(dto.gender());
        if (dto.bloodType()              != null) profile.setBloodType(dto.bloodType());
        if (dto.address()                != null) profile.setAddress(dto.address());
        if (dto.emergencyContactName()   != null) profile.setEmergencyContactName(dto.emergencyContactName());
        if (dto.emergencyContactPhone()  != null) profile.setEmergencyContactPhone(dto.emergencyContactPhone());

        return patientProfileMapper.toDto(patientProfileRepository.save(profile));
    }

    private PatientProfile findProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return patientProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
    }
}
