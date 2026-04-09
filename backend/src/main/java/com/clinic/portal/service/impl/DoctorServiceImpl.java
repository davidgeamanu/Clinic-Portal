package com.clinic.portal.service.impl;

import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
        return doctorProfileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO updateProfile(Long userId, DoctorProfileUpdateDTO dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        if (dto.biography() != null)
            profile.setBiography(dto.biography());
        if (dto.consultationFee() != null)
            profile.setConsultationFee(dto.consultationFee());

        // Replace specialization list if provided sending an empty list clears them all
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
    public List<DoctorProfileResponseDTO> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    @Override
    public List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND));
        return doctorProfileRepository.findBySpecializationsContaining(specialization)
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    private DoctorProfile findProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        return doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
    }
}
