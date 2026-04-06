package com.clinic.portal.service;

import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;

import java.util.List;

public interface DoctorService {

    DoctorProfileResponseDTO getProfile(Long userId); // by User ID (for /me)

    DoctorProfileResponseDTO getProfileById(Long profileId); // by DoctorProfile ID (for /{id})

    DoctorProfileResponseDTO updateProfile(Long userId, DoctorProfileUpdateDTO dto);

    List<DoctorProfileResponseDTO> getAllDoctors();

    List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId);
}
