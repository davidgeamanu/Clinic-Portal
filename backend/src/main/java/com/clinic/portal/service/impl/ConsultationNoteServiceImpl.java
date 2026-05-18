package com.clinic.portal.service.impl;

import com.clinic.portal.dto.consultation.ConsultationNoteRequestDTO;
import com.clinic.portal.dto.consultation.ConsultationNoteResponseDTO;
import com.clinic.portal.dto.consultation.DoctorNoteListItemDTO;
import com.clinic.portal.dto.consultation.MedicalDocumentResponseDTO;
import com.clinic.portal.dto.consultation.PatientHistoryItemDTO;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.exception.UnauthorizedException;
import com.clinic.portal.mapper.ConsultationNoteMapper;
import com.clinic.portal.mapper.MedicalDocumentMapper;
import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.NotificationType;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.MedicalDocumentRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.ConsultationNoteService;
import com.clinic.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ConsultationNoteMapper consultationNoteMapper;
    private final MedicalDocumentMapper medicalDocumentMapper;

    @Value("${app.storage.location}")
    private String storageLocation;

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
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        // Notes can be written once the consultation is in progress or completed
        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_CANNOT_BE_MODIFIED);
        }

        // Prevent duplicate notes
        if (consultationNoteRepository.findByAppointment(appointment).isPresent()) {
            throw new DuplicateDataException(ExceptionCode.CONSULTATION_NOTE_ALREADY_EXISTS);
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
    @Transactional
    public ConsultationNoteResponseDTO updateNote(Long appointmentId, ConsultationNoteRequestDTO dto, Long doctorUserId) {
        Appointment appointment = findAppointment(appointmentId);

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        ConsultationNote note = consultationNoteRepository.findByAppointment(appointment)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));

        note.setDiagnosis(dto.diagnosis());
        note.setTreatment(dto.treatment());
        note.setPrescription(dto.prescription());
        note.setNotes(dto.notes());

        return consultationNoteMapper.toDto(consultationNoteRepository.save(note));
    }

    @Override
    public ConsultationNoteResponseDTO getNoteByAppointment(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        ConsultationNote note = consultationNoteRepository.findByAppointment(appointment)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));
        return consultationNoteMapper.toDto(note);
    }

    @Override
    @Transactional
    public MedicalDocumentResponseDTO uploadDocument(Long noteId, MultipartFile file, Long doctorUserId) {
        ConsultationNote note = consultationNoteRepository.findById(noteId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));

        // Verify the doctor uploading owns this note's appointment
        if (!note.getAppointment().getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ExceptionCode.INVALID_FILE_TYPE);
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

    @Override
    public List<PatientHistoryItemDTO> getPatientHistory(Long patientProfileId, Long requestingDoctorUserId) {
        User doctorUser = userRepository.findById(requestingDoctorUserId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        DoctorProfile doctor = doctorProfileRepository.findByUser(doctorUser)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
        com.clinic.portal.model.PatientProfile patient = patientProfileRepository.findById(patientProfileId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));

        if (!appointmentRepository.existsByDoctorAndPatient(doctor, patient)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        return consultationNoteRepository
                .findByAppointment_Patient_IdOrderByAppointment_ScheduledAtDesc(patientProfileId)
                .stream()
                .map(note -> {
                    User du = note.getAppointment().getDoctor().getUser();
                    List<MedicalDocumentResponseDTO> docs = medicalDocumentRepository
                            .findByConsultationNote(note)
                            .stream()
                            .map(medicalDocumentMapper::toDto)
                            .toList();
                    return new PatientHistoryItemDTO(
                            note.getId(),
                            note.getAppointment().getId(),
                            note.getAppointment().getScheduledAt().toLocalDate().toString(),
                            du.getFirstName() + " " + du.getLastName(),
                            note.getDiagnosis(),
                            note.getTreatment(),
                            note.getPrescription(),
                            note.getNotes(),
                            docs
                    );
                })
                .toList();
    }

    @Override
    public List<PatientHistoryItemDTO> getMyMedicalRecords(Long patientUserId) {
        User patientUser = userRepository.findById(patientUserId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        PatientProfile patient = patientProfileRepository.findByUser(patientUser)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));

        return consultationNoteRepository
                .findByAppointment_Patient_IdOrderByAppointment_ScheduledAtDesc(patient.getId())
                .stream()
                .map(note -> {
                    User du = note.getAppointment().getDoctor().getUser();
                    List<MedicalDocumentResponseDTO> docs = medicalDocumentRepository
                            .findByConsultationNote(note)
                            .stream()
                            .map(medicalDocumentMapper::toDto)
                            .toList();
                    return new PatientHistoryItemDTO(
                            note.getId(),
                            note.getAppointment().getId(),
                            note.getAppointment().getScheduledAt().toLocalDate().toString(),
                            du.getFirstName() + " " + du.getLastName(),
                            note.getDiagnosis(),
                            note.getTreatment(),
                            note.getPrescription(),
                            note.getNotes(),
                            docs
                    );
                })
                .toList();
    }

    @Override
    public List<DoctorNoteListItemDTO> getDoctorNotes(Long doctorUserId) {
        return consultationNoteRepository
                .findByAppointment_Doctor_User_IdOrderByAppointment_ScheduledAtDesc(doctorUserId)
                .stream()
                .map(note -> {
                    User pu = note.getAppointment().getPatient().getUser();
                    List<MedicalDocumentResponseDTO> docs = medicalDocumentRepository
                            .findByConsultationNote(note)
                            .stream()
                            .map(medicalDocumentMapper::toDto)
                            .toList();
                    return new DoctorNoteListItemDTO(
                            note.getId(),
                            note.getAppointment().getId(),
                            note.getAppointment().getPatient().getId(),
                            pu.getFirstName() + " " + pu.getLastName(),
                            note.getAppointment().getScheduledAt().toLocalDate().toString(),
                            note.getDiagnosis(),
                            note.getTreatment(),
                            note.getPrescription(),
                            note.getNotes(),
                            docs
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteDocument(Long noteId, Long documentId, Long doctorUserId) {
        MedicalDocument document = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.MEDICAL_DOCUMENT_NOT_FOUND));

        if (!document.getConsultationNote().getId().equals(noteId)) {
            throw new DataNotFoundException(ExceptionCode.MEDICAL_DOCUMENT_NOT_FOUND);
        }

        if (!document.getConsultationNote().getAppointment().getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        try {
            Files.deleteIfExists(Paths.get(document.getStoragePath()));
        } catch (IOException ignored) {
            // File already gone — still remove the DB record
        }

        medicalDocumentRepository.delete(document);
    }

    @Override
    public Resource downloadDocument(Long noteId, Long documentId, Long requestingUserId) {
        MedicalDocument document = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.MEDICAL_DOCUMENT_NOT_FOUND));

        if (!document.getConsultationNote().getId().equals(noteId)) {
            throw new DataNotFoundException(ExceptionCode.MEDICAL_DOCUMENT_NOT_FOUND);
        }

        // Allow access to the patient of the appointment or any doctor who has treated them
        Long patientUserId = document.getConsultationNote().getAppointment().getPatient().getUser().getId();
        boolean isPatient = requestingUserId.equals(patientUserId);
        boolean isDoctor = doctorProfileRepository
                .findByUser(userRepository.findById(requestingUserId)
                        .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND)))
                .map(doc -> appointmentRepository.existsByDoctorAndPatient(
                        doc, document.getConsultationNote().getAppointment().getPatient()))
                .orElse(false);

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        Path filePath = Paths.get(document.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new DataNotFoundException(ExceptionCode.MEDICAL_DOCUMENT_NOT_FOUND);
        }

        return new FileSystemResource(filePath);
    }

    /**
     * Saves the uploaded file to disk and returns the storage path.
     * The filename is a UUID
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
            throw new BusinessException(ExceptionCode.FILE_STORAGE_ERROR);
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
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }
}