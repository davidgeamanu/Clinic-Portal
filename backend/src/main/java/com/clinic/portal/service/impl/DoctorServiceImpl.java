package com.clinic.portal.service.impl;

import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.exception.ResourceNotFoundException;
import com.clinic.portal.mapper.DoctorProfileMapper;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorProfileMapper doctorProfileMapper;

    @Override
    public DoctorProfileResponseDTO getProfile(Long userId) {
        return doctorProfileMapper.toDto(findProfileByUserId(userId));
    }

    @Override
    public DoctorProfileResponseDTO getProfileById(Long profileId) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return doctorProfileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO updateProfile(Long userId, DoctorProfileUpdateDTO dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        if (dto.biography()       != null) profile.setBiography(dto.biography());
        if (dto.consultationFee() != null) profile.setConsultationFee(dto.consultationFee());

        // Replace specialization list if provided — sending an empty list clears them all
        if (dto.specializationIds() != null) {
            List<Specialization> specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Specialization not found: " + id)))
                    .toList();
            profile.setSpecializations(specializations);
        }

        return doctorProfileMapper.toDto(doctorProfileRepository.save(profile));
    }

    @Override
    public List<DoctorProfileResponseDTO> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    @Override
    public List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found"));
        return doctorProfileRepository.findBySpecializationsContaining(specialization)
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    private DoctorProfile findProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
    }
}
