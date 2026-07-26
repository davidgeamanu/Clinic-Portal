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
import com.clinic.portal.model.DoctorAvailability;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorAvailabilityRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
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

        validateWithinWorkingHours(doctor, dto.scheduledAt(), dto.durationMinutes());

        // Half-open overlap check: [newStart, newEnd) vs [existingStart, existingEnd).
        // Two ranges overlap iff existingStart < newEnd AND existingEnd > newStart.
        // The scan window starts a day early so appointments that begin before
        // midnight but overlap the requested slot are still caught.
        LocalDateTime newStart = dto.scheduledAt();
        LocalDateTime newEnd = newStart.plusMinutes(dto.durationMinutes());
        LocalDateTime windowStart = newStart.minusDays(1);

        boolean conflict = appointmentRepository
                .findByDoctor_IdAndScheduledAtBetweenAndStatusNotIn(
                        doctor.getId(), windowStart, newEnd,
                        EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW))
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

        // The check above is advisory only — under concurrency two requests can
        // both pass it. The database exclusion constraint is the real guarantee;
        // flush here so its violation surfaces inside this method.
        Appointment saved;
        try {
            saved = appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDataException(ExceptionCode.APPOINTMENT_SLOT_TAKEN);
        }

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

        // Recompute doctor's aggregate rating with a single AVG query.
        DoctorProfile doctor = appointment.getDoctor();
        Double avg = appointmentRepository.findAverageRatingForDoctor(doctor);
        doctor.setRating(avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : null);
        doctorProfileRepository.save(doctor);

        return appointmentMapper.toDto(appointment);
    }

    /**
     * Doctors who configured working hours only accept appointments that fit
     * entirely inside one of their windows. Doctors with no configured hours
     * keep the legacy behaviour and accept any future slot.
     */
    private void validateWithinWorkingHours(DoctorProfile doctor, LocalDateTime start, int durationMinutes) {
        List<DoctorAvailability> windows = doctorAvailabilityRepository
                .findByDoctor_IdOrderByDayOfWeekAscStartTimeAsc(doctor.getId());
        if (windows.isEmpty()) return;

        LocalDateTime end = start.plusMinutes(durationMinutes);
        boolean fits = start.toLocalDate().equals(end.toLocalDate())
                && windows.stream()
                .filter(w -> w.getDayOfWeek() == start.getDayOfWeek())
                .anyMatch(w -> !start.toLocalTime().isBefore(w.getStartTime())
                        && !end.toLocalTime().isAfter(w.getEndTime()));

        if (!fits) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_OUTSIDE_WORKING_HOURS);
        }
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
