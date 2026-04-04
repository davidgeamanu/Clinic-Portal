package com.clinic.portal.dto.patient;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;

import java.time.LocalDate;

public record PatientProfileResponseDTO(
        Long id,
        Long userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth,
        Gender gender,
        BloodType bloodType,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {}
