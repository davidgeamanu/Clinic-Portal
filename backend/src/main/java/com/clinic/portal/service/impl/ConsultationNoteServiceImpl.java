package com.clinic.portal.service.impl;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.exception.ConflictException;
import com.clinic.portal.exception.InvalidOperationException;
import com.clinic.portal.exception.ResourceNotFoundException;
import com.clinic.portal.exception.UnauthorizedException;
import com.clinic.portal.mapper.ConsultationNoteMapper;
import com.clinic.portal.mapper.MedicalDocumentMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.MedicalDocument;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.NotificationType;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.MedicalDocumentRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.ConsultationNoteService;
import com.clinic.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsultationNoteServiceImpl implements ConsultationNoteService {

    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ConsultationNoteMapper consultationNoteMapper;
    private final MedicalDocumentMapper medicalDocumentMapper;

    @Value("${app.storage.location}")
    private String storageLocation;

    /** Accepted MIME types — restrict what doctors can upload */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    @Transactional
    public ConsultationNoteResponseDTO createNote(Long appointmentId, ConsultationNoteRequestDTO dto, Long doctorUserId) {
        Appointment appointment = findAppointment(appointmentId);

        // Verify this doctor owns the appointment
        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedException("You are not the doctor for this appointment");
        }

        // Notes can only be written for completed appointments
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidOperationException("Consultation notes can only be added to COMPLETED appointments");
        }

        // Prevent duplicate notes
        if (consultationNoteRepository.findByAppointment(appointment).isPresent()) {
            throw new ConflictException("A consultation note already exists for this appointment");
        }

        ConsultationNote note = ConsultationNote.builder()
                .appointment(appointment)
                .diagnosis(dto.diagnosis())
                .treatment(dto.treatment())
                .prescription(dto.prescription())
                .notes(dto.notes())
                .build();

        ConsultationNote saved = consultationNoteRepository.save(note);

        // Notify the patient their consultation notes are ready
        User patientUser = appointment.getPatient().getUser();
        notificationService.send(
                patientUser,
                "Your consultation notes are available for appointment on " + appointment.getScheduledAt(),
                NotificationType.CONSULTATION_NOTE_ADDED,
                saved.getId()
        );

        return consultationNoteMapper.toDto(saved);
    }

    @Override
    public ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        ConsultationNote note = consultationNoteRepository.findByAppointment(appointment)
                .orElseThrow(() -> new ResourceNotFoundException("No consultation note found for this appointment"));
        return consultationNoteMapper.toDto(note);
    }

    @Override
    @Transactional
    public MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file, Long doctorUserId) {
        ConsultationNote note = consultationNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation note not found"));

        // Verify the doctor uploading owns this note's appointment
        if (!note.getAppointment().getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedException("You are not the doctor for this consultation");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidOperationException("File type not allowed. Accepted: PDF, images, Word documents");
        }

        String storagePath = saveFileToDisk(file, noteId);

        MedicalDocument document = MedicalDocument.builder()
                .consultationNote(note)
                .originalFileName(file.getOriginalFilename())
                .contentType(contentType)
                .storagePath(storagePath)
                .fileSizeBytes(file.getSize())
                .build();

        return medicalDocumentMapper.toDto(medicalDocumentRepository.save(document));
    }

    /**
     * Saves the uploaded file to disk and returns the storage path.
     * The filename is a UUID — never use the original filename as a path component
     * (path traversal vulnerability: attacker could upload a file named "../../etc/passwd").
     */
    private String saveFileToDisk(MultipartFile file, Long noteId) {
        try {
            Path directory = Paths.get(storageLocation, "consultation-" + noteId);
            Files.createDirectories(directory);

            String extension = getExtension(file.getOriginalFilename());
            String safeFileName = UUID.randomUUID() + extension;
            Path targetPath = directory.resolve(safeFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to store file: " + e.getMessage());
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }
}