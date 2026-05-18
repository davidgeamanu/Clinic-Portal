package com.clinic.portal.service.impl;

import com.clinic.portal.dto.appointment.AppointmentRatingRequestDTO;
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
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.repository.RoomRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final AppointmentMapper appointmentMapper;

    /**
     * Defines which status transitions are legal.
     * Any transition not in this map is rejected by validateTransition().
     */
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> VALID_TRANSITIONS = Map.of(
            AppointmentStatus.SCHEDULED,   Set.of(AppointmentStatus.CONFIRMED,   AppointmentStatus.CANCELLED),
            AppointmentStatus.CONFIRMED,   Set.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED),
            AppointmentStatus.IN_PROGRESS, Set.of(AppointmentStatus.COMPLETED)
    );

    @Override
    @Transactional
    public AppointmentResponseDTO bookAppointment(Long patientUserId, AppointmentRequestDTO dto) {
        User patientUser = findUser(patientUserId);
        PatientProfile patient = patientProfileRepository.findByUser(patientUser)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.doctorId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));

        // Half-open overlap check: [newStart, newEnd) vs [existingStart, existingEnd).
        // Two ranges overlap iff existingStart < newEnd AND existingEnd > newStart.
        // We scan only same-day appointments to keep the result set small.
        LocalDateTime newStart = dto.scheduledAt();
        LocalDateTime newEnd = newStart.plusMinutes(dto.durationMinutes());
        LocalDateTime dayStart = newStart.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = newStart.toLocalDate().atTime(LocalTime.MAX);

        boolean conflict = appointmentRepository
                .findByDoctor_IdAndScheduledAtBetweenAndStatusNot(
                        doctor.getId(), dayStart, dayEnd, AppointmentStatus.CANCELLED)
                .stream()
                .anyMatch(a -> a.getScheduledAt().isBefore(newEnd)
                        && a.getScheduledAt().plusMinutes(a.getDurationMinutes()).isAfter(newStart));

        if (conflict) {
            throw new DuplicateDataException(ExceptionCode.APPOINTMENT_SLOT_TAKEN);
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(dto.scheduledAt())
                .durationMinutes(dto.durationMinutes())
                .mode(dto.mode() != null ? dto.mode() : AppointmentMode.IN_PERSON)
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
        if (!requestingUserId.equals(patientUserId) && !requestingUserId.equals(doctorUserId)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        if (dto.status() == AppointmentStatus.IN_PROGRESS) {
            long minutesUntilStart = ChronoUnit.MINUTES.between(LocalDateTime.now(), appointment.getScheduledAt());
            if (minutesUntilStart > 0) {
                throw new BusinessException(ExceptionCode.APPOINTMENT_NOT_YET_STARTABLE);
            }
        }

        validateTransition(appointment.getStatus(), dto.status());
        appointment.setStatus(dto.status());
        if (dto.status() == AppointmentStatus.IN_PROGRESS) {
            appointment.setStartedAt(LocalDateTime.now());
        } else if (dto.status() == AppointmentStatus.COMPLETED) {
            appointment.setCompletedAt(LocalDateTime.now());
        }
        Appointment saved = appointmentRepository.save(appointment);

        updateRoomStatus(saved);

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

    @Override
    @Transactional
    public AppointmentResponseDTO rateAppointment(Long appointmentId, AppointmentRatingRequestDTO dto) {
        Appointment appointment = findAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_NOT_COMPLETED);
        }
        if (appointment.getRating() != null) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_ALREADY_RATED);
        }

        appointment.setRating(dto.rating());
        appointment.setReview(dto.review());
        appointmentRepository.save(appointment);

        // Recompute doctor's aggregate rating across all rated appointments.
        DoctorProfile doctor = appointment.getDoctor();
        OptionalDouble avg = appointmentRepository.findByDoctorOrderByScheduledAtDesc(doctor).stream()
                .map(Appointment::getRating)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        doctor.setRating(avg.isPresent() ? BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
        doctorProfileRepository.save(doctor);

        return appointmentMapper.toDto(appointment);
    }

    private void updateRoomStatus(Appointment appointment) {
        if (appointment.getMode() != AppointmentMode.IN_PERSON) return;
        var room = appointment.getDoctor().getRoom();
        if (room == null) return;
        boolean occupied = appointment.getStatus() == AppointmentStatus.CONFIRMED
                || appointment.getStatus() == AppointmentStatus.IN_PROGRESS;
        room.setStatus(occupied ? RoomStatus.OCCUPIED : RoomStatus.FREE);
        roomRepository.save(room);
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus next) {
        Set<AppointmentStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(next)) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_CANNOT_BE_MODIFIED);
        }
    }

    private NotificationType notificationTypeFor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED   -> NotificationType.APPOINTMENT_CONFIRMED;
            case IN_PROGRESS -> NotificationType.APPOINTMENT_STARTED;
            case CANCELLED   -> NotificationType.APPOINTMENT_CANCELLED;
            default          -> NotificationType.GENERAL;
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
