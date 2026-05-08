package com.clinic.portal.dto.admin;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;

import java.time.LocalDate;

public record AdminPatientDTO(
        Long patientProfileId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        boolean active,
        LocalDate dateOfBirth,
        Gender gender,
        BloodType bloodType,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {}