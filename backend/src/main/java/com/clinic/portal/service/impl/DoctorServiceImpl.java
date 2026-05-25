package com.clinic.portal.service.impl;

import com.clinic.portal.dto.doctor.BookedSlotDTO;
import com.clinic.portal.dto.doctor.DoctorPatientListItemDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.doctor.PatientSummaryDTO;
import com.clinic.portal.dto.doctor.RecentPatientDTO;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.mapper.DoctorProfileMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final DoctorProfileMapper doctorProfileMapper;

    @Override
    public DoctorProfileResponseDTO getProfile(Long userId) {
        DoctorProfile profile = findProfileByUserId(userId);

        List<Appointment> allAppointments = appointmentRepository.findByDoctorOrderByScheduledAtDesc(profile);

        List<Appointment> timed = allAppointments.stream()
                .filter(a -> a.getStartedAt() != null && a.getCompletedAt() != null)
                .toList();

        Double avg = timed.isEmpty() ? null :
                timed.stream()
                        .mapToLong(a -> ChronoUnit.MINUTES.between(a.getStartedAt(), a.getCompletedAt()))
                        .average()
                        .orElse(0);

        long completedPatientCount = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(a -> a.getPatient().getId())
                .distinct()
                .count();

        return doctorProfileMapper.toDto(profile, avg, completedPatientCount);
    }

    @Override
    public DoctorProfileResponseDTO getProfileById(Long profileId) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
        return doctorProfileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO updateProfile(Long userId, DoctorProfileUpdateDTO dto) {
        DoctorProfile profile = findProfileByUserId(userId);

        if (dto.biography() != null)
            profile.setBiography(dto.biography());
        if (dto.consultationFee() != null)
            profile.setConsultationFee(dto.consultationFee());
        if (dto.rating() != null)
            profile.setRating(dto.rating());

        // Replace specialization list if provided sending an empty list clears them all
        if (dto.specializationIds() != null) {
            List<Specialization> specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND)))
                    .collect(Collectors.toCollection(ArrayList::new));
            profile.setSpecializations(specializations);
        }

        return doctorProfileMapper.toDto(doctorProfileRepository.save(profile));
    }

    @Override
    public List<DoctorProfileResponseDTO> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    @Override
    public List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND));
        return doctorProfileRepository.findBySpecializationsContaining(specialization)
                .stream().map(doctorProfileMapper::toDto).toList();
    }

    @Override
    public List<RecentPatientDTO> getRecentPatients(Long doctorUserId) {
        DoctorProfile doctor = findProfileByUserId(doctorUserId);

        List<Appointment> completed = appointmentRepository
                .findByDoctorAndStatusOrderByScheduledAtDesc(doctor, AppointmentStatus.COMPLETED);

        List<RecentPatientDTO> result = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();

        for (Appointment apt : completed) {
            Long patientId = apt.getPatient().getId();
            if (!seen.add(patientId)) continue;

            Optional<ConsultationNote> note = consultationNoteRepository.findByAppointment(apt);
            PatientProfile patient = apt.getPatient();

            result.add(new RecentPatientDTO(
                    patient.getId(),
                    patient.getUser().getFirstName(),
                    patient.getUser().getLastName(),
                    apt.getScheduledAt().toLocalDate().toString(),
                    note.map(ConsultationNote::getDiagnosis).orElse(null),
                    note.map(ConsultationNote::getNotes).orElse(null)
            ));

            if (result.size() >= 3) break;
        }

        return result;
    }

    @Override
    public List<DoctorPatientListItemDTO> getDoctorPatients(Long doctorUserId) {
        DoctorProfile doctor = findProfileByUserId(doctorUserId);

        // All non-cancelled appointments, newest first
        List<Appointment> appointments = appointmentRepository
                .findByDoctorOrderByScheduledAtDesc(doctor)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();

        // First occurrence per patient = most recent non-cancelled appointment (LinkedHashMap preserves order)
        Map<Long, Appointment> latestByPatient = new LinkedHashMap<>();
        // First completed occurrence per patient = most recent completed appointment
        Map<Long, Appointment> latestCompletedByPatient = new HashMap<>();
        Map<Long, Integer> countByPatient = new HashMap<>();

        for (Appointment apt : appointments) {
            Long pid = apt.getPatient().getId();
            latestByPatient.putIfAbsent(pid, apt);
            if (apt.getStatus() == AppointmentStatus.COMPLETED) {
                latestCompletedByPatient.putIfAbsent(pid, apt);
                countByPatient.merge(pid, 1, Integer::sum);
            }
        }

        List<DoctorPatientListItemDTO> result = new ArrayList<>();

        for (Map.Entry<Long, Appointment> entry : latestByPatient.entrySet()) {
            Long pid = entry.getKey();
            PatientProfile patient = entry.getValue().getPatient();
            User user = patient.getUser();

            Appointment lastCompleted = latestCompletedByPatient.get(pid);

            String lastDiagnosis = Optional.ofNullable(lastCompleted)
                    .flatMap(consultationNoteRepository::findByAppointment)
                    .map(ConsultationNote::getDiagnosis)
                    .orElse(null);

            String lastVisitDate = Optional.ofNullable(lastCompleted)
                    .map(a -> a.getScheduledAt().toLocalDate().toString())
                    .orElse(null);

            result.add(new DoctorPatientListItemDTO(
                    patient.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null,
                    patient.getGender() != null ? patient.getGender().name() : null,
                    patient.getBloodType() != null ? patient.getBloodType().name() : null,
                    user.getEmail(),
                    user.getPhoneNumber(),
                    lastVisitDate,
                    lastDiagnosis,
                    countByPatient.getOrDefault(pid, 0)
            ));
        }

        return result;
    }

    @Override
    public PatientSummaryDTO getPatientSummary(Long doctorUserId, Long patientProfileId) {
        DoctorProfile doctor = findProfileByUserId(doctorUserId);
        PatientProfile patient = patientProfileRepository.findById(patientProfileId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND));

        if (!appointmentRepository.existsByDoctorAndPatient(doctor, patient)) {
            throw new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND);
        }

        User user = patient.getUser();
        return new PatientSummaryDTO(
                patient.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null,
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getBloodType() != null ? patient.getBloodType().name() : null,
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getHeightCm(),
                patient.getWeightKg(),
                patient.getAllergies(),
                patient.getChronicConditions(),
                patient.getFamilyHistory(),
                patient.getLifestyleSmoking(),
                patient.getLifestyleAlcohol(),
                patient.getLifestyleExercise(),
                patient.getLifestyleDiet()
        );
    }

    @Override
    public List<BookedSlotDTO> getBookedSlots(Long doctorProfileId, LocalDate date) {
        if (!doctorProfileRepository.existsById(doctorProfileId))
            throw new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        return appointmentRepository
                .findByDoctor_IdAndScheduledAtBetweenAndStatusNot(
                        doctorProfileId, dayStart, dayEnd, AppointmentStatus.CANCELLED)
                .stream()
                .map(a -> new BookedSlotDTO(a.getScheduledAt(), a.getDurationMinutes()))
                .toList();
    }

    private DoctorProfile findProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        return doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));
    }
}
