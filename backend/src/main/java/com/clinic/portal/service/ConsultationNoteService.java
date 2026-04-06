package com.clinic.portal.service;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ConsultationNoteService {
    ConsultationNoteResponseDTO createNote(Long appointmentId, ConsultationNoteRequestDTO dto, Long doctorUserId);

    ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId);

    MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file, Long doctorUserId);
}
