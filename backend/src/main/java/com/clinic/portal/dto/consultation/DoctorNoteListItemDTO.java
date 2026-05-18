package com.clinic.portal.dto.consultation;

import java.util.List;

public record DoctorNoteListItemDTO(
        Long noteId,
        Long appointmentId,
        Long patientProfileId,
        String patientName,
        String appointmentDate,
        String diagnosis,
        String treatment,
        String prescription,
        String notes,
        List<MedicalDocumentResponseDTO> documents) {}