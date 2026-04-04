package com.clinic.portal.dto.consultation;

import java.time.LocalDateTime;

/**
 * Metadata about an uploaded document — never the file bytes.
 * The frontend uses id to construct a download URL: GET /documents/{id}/download
 */
public record MedicalDocumentResponseDTO(
        Long id,
        String originalFileName,
        String contentType,
        Long fileSizeBytes,
        LocalDateTime createdAt
) {}
