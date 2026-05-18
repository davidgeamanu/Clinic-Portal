package com.clinic.portal.dto.consultation;

import java.util.List;

public record PatientHistoryItemDTO(
        Long noteId,
        Long appointmentId,
        String appointmentDate,
        String doctorName,
        String diagnosis,
        String treatment,
        String prescription,
        String notes,
        List<MedicalDocumentResponseDTO> documents) {}