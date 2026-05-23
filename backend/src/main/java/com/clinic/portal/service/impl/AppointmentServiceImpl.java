package com.clinic.portal.service.impl;

import com.clinic.portal.dto.appointment.AppointmentRatingRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.mapper.AppointmentMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.AppointmentService;
import com.clinic.portal.service.appointment.state.AppointmentState;
import com.clinic.portal.service.appointment.state.AppointmentStateRegistry;
import com.clinic.portal.service.appointment.state.EntryHook;
import com.clinic.portal.service.appointment.state.EntryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AppointmentStateRegistry stateRegistry;

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

        eventPublisher.publishEvent(new AppointmentBookedEvent(saved.getId()));

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

        // Authorization (role + appointment ownership) is enforced upstream via
        // @authz.canUpdateAppointmentStatus on the controller, or by the caller
        // (e.g. AppointmentExpirationJob) when invoked from a scheduled task.

        AppointmentState targetState = stateRegistry.forStatus(dto.status());
        if (targetState instanceof EntryValidator validator) {
            validator.validateEntry(appointment);
        }

        AppointmentStatus previousStatus = appointment.getStatus();
        AppointmentState currentState = stateRegistry.forStatus(previousStatus);
        if (!currentState.allowedNextStates().contains(dto.status())) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_CANNOT_BE_MODIFIED);
        }

        appointment.setStatus(dto.status());
        if (targetState instanceof EntryHook hook) {
            hook.onEntry(appointment);
        }
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publishEvent(new AppointmentStatusChangedEvent(
                saved.getId(), previousStatus, dto.status(), requestingUserId));

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

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
    }
}
