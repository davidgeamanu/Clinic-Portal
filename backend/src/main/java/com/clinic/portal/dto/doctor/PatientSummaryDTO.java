package com.clinic.portal.dto.doctor;

import java.math.BigDecimal;

public record PatientSummaryDTO(
        Long patientProfileId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String dateOfBirth,
        String gender,
        String bloodType,
        String emergencyContactName,
        String emergencyContactPhone,
        Integer heightCm,
        BigDecimal weightKg,
        String allergies,
        String chronicConditions,
        String familyHistory,
        String lifestyleSmoking,
        String lifestyleAlcohol,
        String lifestyleExercise,
        String lifestyleDiet) {}
