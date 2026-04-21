package com.clinic.portal.service.impl;

import com.clinic.portal.dto.appointment.AppointmentRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.exception.UnauthorizedException;
import com.clinic.portal.mapper.AppointmentMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.NotificationType;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.AppointmentService;
import com.clinic.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AppointmentMapper appointmentMapper;

    /**
     * Defines which status transitions are legal.
     * Any transition not in this map is rejected by validateTransition().
     */
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> VALID_TRANSITIONS = Map.of(
            AppointmentStatus.SCHEDULED, Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED),
            AppointmentStatus.CONFIRMED, Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED)
    );

    @Override
    @Transactional
    public AppointmentResponseDTO bookAppointment(Long patientUserId, AppointmentRequestDTO dto) {
        User patientUser = findUser(patientUserId);
        PatientProfile patient = patientProfileRepository.findByUser(patientUser)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.doctorId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));

        //reject if doctor has any active appointment overlapping this window
        LocalDateTime endTime = dto.scheduledAt().plusMinutes(dto.durationMinutes());
        if (appointmentRepository.existsByDoctorAndScheduledAtBetweenAndStatusNot(
                doctor, dto.scheduledAt(), endTime, AppointmentStatus.CANCELLED)) {
            throw new DuplicateDataException(ExceptionCode.APPOINTMENT_SLOT_TAKEN);
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(dto.scheduledAt())
                .durationMinutes(dto.durationMinutes())
                .reason(dto.reason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        notificationService.send(
                doctor.getUser(),
                "New appointment booked by " + patientUser.getFirstName() + " " + patientUser.getLastName()
                        + " on " + dto.scheduledAt(),
                NotificationType.APPOINTMENT_SCHEDULED,
                saved.getId()
        );

        return appointmentMapper.toDto(saved);
    }

    @Override
    public AppointmentResponseDTO getById(Long appointmentId) {
        return appointmentMapper.toDto(findAppointment(appointmentId));
    }

    @Override
    public List<AppointmentResponseDTO> getPatientAppointments(Long patientUserId) {
        User user = findUser(patientUserId);
        PatientProfile patient = patientProfileRepository.findByUser(user)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));
        return appointmentRepository.findByPatientOrderByScheduledAtDesc(patient)
                .stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    public List<AppointmentResponseDTO> getDoctorAppointments(Long doctorUserId) {
        User user = findUser(doctorUserId);
        DoctorProfile doctor = doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
        return appointmentRepository.findByDoctorOrderByScheduledAtAsc(doctor)
                .stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateStatus(Long appointmentId, AppointmentStatusUpdateDTO dto, Long requestingUserId) {
        Appointment appointment = findAppointment(appointmentId);

        // Only the patient or doctor involved in this appointment may change its status
        Long patientUserId = appointment.getPatient().getUser().getId();
        Long doctorUserId = appointment.getDoctor().getUser().getId();
        boolean isDoctor = requestingUserId.equals(doctorUserId);
        boolean isPatient = requestingUserId.equals(patientUserId);

        if (!isDoctor && !isPatient) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        // CONFIRMED and COMPLETED are doctor-only actions
        if ((dto.status() == AppointmentStatus.CONFIRMED || dto.status() == AppointmentStatus.COMPLETED) && !isDoctor) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        validateTransition(appointment.getStatus(), dto.status());
        appointment.setStatus(dto.status());
        Appointment saved = appointmentRepository.save(appointment);

        // Notify the other party about the status change
        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();
        User recipient = requestingUserId.equals(patientUserId) ? doctorUser : patientUser;

        notificationService.send(
                recipient,
                "Appointment on " + appointment.getScheduledAt() + " has been " + dto.status().name().toLowerCase(),
                notificationTypeFor(dto.status()),
                saved.getId()
        );

        return appointmentMapper.toDto(saved);
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus next) {
        Set<AppointmentStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(next)) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_CANNOT_BE_MODIFIED);
        }
    }

    private NotificationType notificationTypeFor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.APPOINTMENT_CONFIRMED;
            case CANCELLED -> NotificationType.APPOINTMENT_CANCELLED;
            default -> NotificationType.GENERAL;
        };
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
    }
}
