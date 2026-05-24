package com.clinic.portal.controller.consultation;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.DoctorNoteListItemDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.dto.consultation.PatientHistoryItemDTO;
import com.clinic.portal.controller.AuthenticatedController;
import com.clinic.portal.service.ConsultationNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/consultation-notes")
@RequiredArgsConstructor
public class ConsultationNoteControllerImpl implements ConsultationNoteController, AuthenticatedController {

    private final ConsultationNoteService consultationNoteService;

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ConsultationNoteResponseDTO createNote(Long appointmentId, ConsultationNoteRequestDTO dto) {
        log.info("[CONSULTATION] Creating note for appointment: {}", appointmentId);
        return consultationNoteService.createNote(appointmentId, dto, currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ConsultationNoteResponseDTO updateNote(Long appointmentId, ConsultationNoteRequestDTO dto) {
        log.info("[CONSULTATION] Updating note for appointment: {}", appointmentId);
        return consultationNoteService.updateNote(appointmentId, dto, currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnAppointment(#appointmentId)")
    public ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId) {
        log.info("[CONSULTATION] Getting note for appointment: {}", appointmentId);
        return consultationNoteService.getNoteByAppointment(appointmentId);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorNoteListItemDTO> getMyNotes() {
        log.info("[CONSULTATION] Getting all notes for doctor: {}", currentUser().getId());
        return consultationNoteService.getDoctorNotes(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public List<PatientHistoryItemDTO> getPatientHistory(Long patientProfileId) {
        log.info("[CONSULTATION] Getting patient history for patientProfileId: {}", patientProfileId);
        return consultationNoteService.getPatientHistory(patientProfileId, currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public List<PatientHistoryItemDTO> getMyMedicalRecords() {
        log.info("[CONSULTATION] Getting medical records for patient user: {}", currentUser().getId());
        return consultationNoteService.getMyMedicalRecords(currentUser().getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDocument(Long noteId, Long documentId) {
        log.info("[CONSULTATION] Downloading document {} for note {}", documentId, noteId);
        Resource resource = consultationNoteService.downloadDocument(noteId, documentId, currentUser().getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(resource.getFilename()).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteDocument(Long noteId, Long documentId) {
        log.info("[CONSULTATION] Deleting document {} from note {}", documentId, noteId);
        consultationNoteService.deleteDocument(noteId, documentId, currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file) {
        log.info("[CONSULTATION] Uploading document for note: {}", noteId);
        return consultationNoteService.uploadDocument(noteId, file, currentUser().getId());
    }

}