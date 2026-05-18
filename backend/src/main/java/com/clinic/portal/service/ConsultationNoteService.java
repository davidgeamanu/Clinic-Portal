package com.clinic.portal.service;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.DoctorNoteListItemDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.dto.consultation.PatientHistoryItemDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConsultationNoteService {
    ConsultationNoteResponseDTO createNote(Long appointmentId, ConsultationNoteRequestDTO dto, Long doctorUserId);

    ConsultationNoteResponseDTO updateNote(Long appointmentId, ConsultationNoteRequestDTO dto, Long doctorUserId);

    ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId);

    MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file, Long doctorUserId);

    List<PatientHistoryItemDTO> getPatientHistory(Long patientProfileId, Long requestingDoctorUserId);

    List<PatientHistoryItemDTO> getMyMedicalRecords(Long patientUserId);

    List<DoctorNoteListItemDTO> getDoctorNotes(Long doctorUserId);

    void deleteDocument(Long noteId, Long documentId, Long doctorUserId);

    Resource downloadDocument(Long noteId, Long documentId, Long requestingUserId);
}
