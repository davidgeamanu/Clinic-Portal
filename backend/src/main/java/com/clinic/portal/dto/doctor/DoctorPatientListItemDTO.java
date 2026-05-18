package com.clinic.portal.dto.doctor;

public record DoctorPatientListItemDTO(
        Long patientProfileId,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String bloodType,
        String email,
        String phoneNumber,
        String lastVisitDate,
        String lastDiagnosis,
        int appointmentCount) {}