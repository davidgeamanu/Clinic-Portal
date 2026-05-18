package com.clinic.portal.dto.doctor;

public record RecentPatientDTO(
        Long patientProfileId,
        String firstName,
        String lastName,
        String lastVisitDate,
        String diagnosis,
        String notes
) {}