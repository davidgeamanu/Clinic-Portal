package com.clinic.portal.service;

import com.clinic.portal.dto.patient.PatientProfileResponseDTO;
import com.clinic.portal.dto.patient.PatientProfileUpdateDTO;

public interface PatientService {

    PatientProfileResponseDTO getProfile(Long userId);

    PatientProfileResponseDTO updateProfile(Long userId, PatientProfileUpdateDTO dto);
}
