package com.clinic.portal.mapper;

import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.model.ConsultationNote;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsultationNoteMapper {

    private final MedicalDocumentMapper documentMapper;

    public ConsultationNoteMapper(MedicalDocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    public ConsultationNoteResponseDTO toDto(ConsultationNote note) {
        List<MedicalDocumentResponseDTO> documents = note.getDocuments()
                .stream()
                .map(documentMapper::toDto)
                .toList();

        return new ConsultationNoteResponseDTO(
                note.getId(),
                note.getAppointment().getId(),
                note.getDiagnosis(),
                note.getTreatment(),
                note.getPrescription(),
                note.getNotes(),
                documents,
                note.getCreatedAt()
        );
    }
}
