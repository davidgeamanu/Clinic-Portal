package com.clinic.portal.dto.consultation;

import java.time.LocalDateTime;

public record MedicalDocumentResponseDTO(
        Long id,
        String originalFileName,
        String contentType,
        Long fileSizeBytes,
        LocalDateTime createdAt
) {}
