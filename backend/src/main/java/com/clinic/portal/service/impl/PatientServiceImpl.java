package com.clinic.portal.service.impl;

import com.clinic.portal.dto.patient.PatientProfileResponseDTO;
import com.clinic.portal.dto.patient.PatientProfileUpdateDTO;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
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

        // Sending null for a field means "leave it as is", not "clear it"
        if (dto.dateOfBirth() != null)
            profile.setDateOfBirth(dto.dateOfBirth());
        if (dto.gender() != null)
            profile.setGender(dto.gender());
        if (dto.bloodType() != null)
            profile.setBloodType(dto.bloodType());
        if (dto.address() != null)
            profile.setAddress(dto.address());
        if (dto.emergencyContactName() != null)
            profile.setEmergencyContactName(dto.emergencyContactName());
        if (dto.emergencyContactPhone() != null)
            profile.setEmergencyContactPhone(dto.emergencyContactPhone());
        if (dto.heightCm() != null)
            profile.setHeightCm(dto.heightCm());
        if (dto.weightKg() != null)
            profile.setWeightKg(dto.weightKg());
        if (dto.allergies() != null)
            profile.setAllergies(dto.allergies());
        if (dto.chronicConditions() != null)
            profile.setChronicConditions(dto.chronicConditions());
        if (dto.familyHistory() != null)
            profile.setFamilyHistory(dto.familyHistory());
        if (dto.lifestyleSmoking() != null)
            profile.setLifestyleSmoking(dto.lifestyleSmoking());
        if (dto.lifestyleAlcohol() != null)
            profile.setLifestyleAlcohol(dto.lifestyleAlcohol());
        if (dto.lifestyleExercise() != null)
            profile.setLifestyleExercise(dto.lifestyleExercise());
        if (dto.lifestyleDiet() != null)
            profile.setLifestyleDiet(dto.lifestyleDiet());

        return patientProfileMapper.toDto(patientProfileRepository.save(profile));
    }

    private PatientProfile findProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        return patientProfileRepository.findByUser(user)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));
    }
}
