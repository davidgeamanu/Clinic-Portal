package com.clinic.portal.mapper;

import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.model.MedicalDocument;
import org.springframework.stereotype.Component;

@Component
public class MedicalDocumentMapper {

    public MedicalDocumentResponseDTO toDto(MedicalDocument document) {
        return new MedicalDocumentResponseDTO(
                document.getId(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSizeBytes(),
                document.getCreatedAt()
        );
    }
}
