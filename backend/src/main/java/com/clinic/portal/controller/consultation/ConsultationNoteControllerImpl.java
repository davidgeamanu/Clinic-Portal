package com.clinic.portal.controller.consultation;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.security.UserDetailsImpl;
import com.clinic.portal.service.ConsultationNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/consultation-notes")
@RequiredArgsConstructor
public class ConsultationNoteControllerImpl implements ConsultationNoteController {

    private final ConsultationNoteService consultationNoteService;

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ConsultationNoteResponseDTO createNote(Long appointmentId, ConsultationNoteRequestDTO dto) {
        log.info("[CONSULTATION] Creating note for appointment: {}", appointmentId);
        return consultationNoteService.createNote(appointmentId, dto, currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnAppointment(#appointmentId)")
    public ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId) {
        log.info("[CONSULTATION] Getting note for appointment: {}", appointmentId);
        return consultationNoteService.getNoteByAppointment(appointmentId);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file) {
        log.info("[CONSULTATION] Uploading document for note: {}", noteId);
        return consultationNoteService.uploadDocument(noteId, file, currentUser().getId());
    }

    private UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}