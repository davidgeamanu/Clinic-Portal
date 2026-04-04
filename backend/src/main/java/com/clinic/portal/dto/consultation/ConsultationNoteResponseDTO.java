package com.clinic.portal.dto.consultation;

import java.time.LocalDateTime;
import java.util.List;

public record ConsultationNoteResponseDTO(
        Long id,
        Long appointmentId,
        String diagnosis,
        String treatment,
        String prescription,
        String notes,
        List<MedicalDocumentResponseDTO> documents,
        LocalDateTime createdAt
) {}
