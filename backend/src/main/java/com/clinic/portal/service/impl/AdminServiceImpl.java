package com.clinic.portal.service.impl;

import com.clinic.portal.dto.admin.AdminAnalyticsDTO;
import com.clinic.portal.dto.admin.AdminDashboardDTO;
import com.clinic.portal.dto.admin.AdminDepartmentDTO;
import com.clinic.portal.dto.admin.AdminPatientDTO;
import com.clinic.portal.dto.admin.DailyStatsDTO;
import com.clinic.portal.dto.admin.DepartmentLoadDTO;
import com.clinic.portal.dto.admin.DoctorRoomAssignmentDTO;
import com.clinic.portal.dto.admin.MonthlyPatientCountDTO;
import com.clinic.portal.dto.admin.MonthlyRevenueDTO;
import com.clinic.portal.dto.admin.RoomResponseDTO;
import com.clinic.portal.dto.admin.RoomUpdateDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.DuplicateDataException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.mapper.AppointmentMapper;
import com.clinic.portal.mapper.DoctorProfileMapper;
import com.clinic.portal.mapper.RoomMapper;
import com.clinic.portal.mapper.SpecializationMapper;
import com.clinic.portal.mapper.UserMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.Room;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.RoomRepository;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final SpecializationMapper specializationMapper;
    private final AppointmentMapper appointmentMapper;
    private final RoomMapper roomMapper;

    @Override
    public AdminDashboardDTO getAdminDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime weekStart = todayStart.minusDays(6);
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        long totalPatients = userRepository.countByRole(Role.PATIENT);
        long activeDoctors = userRepository.countByRoleAndActiveTrue(Role.DOCTOR);
        long todaysAppointments = appointmentRepository.countByScheduledAtBetween(todayStart, todayEnd);

        List<Appointment> completedThisMonth = appointmentRepository
                .findByStatusAndScheduledAtBetween(AppointmentStatus.COMPLETED, monthStart, todayEnd);
        BigDecimal monthlyRevenue = completedThisMonth.stream()
                .map(a -> a.getDoctor().getConsultationFee() != null ? a.getDoctor().getConsultationFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Appointment> weekAppointments = appointmentRepository.findByScheduledAtBetween(weekStart, todayEnd);
        List<User> weekPatients = userRepository.findByRoleAndCreatedAtBetween(Role.PATIENT, weekStart, todayEnd);
        List<DailyStatsDTO> weeklyStats = buildWeeklyStats(weekAppointments, weekPatients, todayStart);

        List<Object[]> deptRaw = appointmentRepository.countAppointmentsBySpecialization();
        List<DepartmentLoadDTO> departmentLoad = buildDepartmentLoad(deptRaw);

        return new AdminDashboardDTO(totalPatients, activeDoctors, todaysAppointments, monthlyRevenue, weeklyStats, departmentLoad);
    }

    @Override
    public AdminAnalyticsDTO getAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime sixMonthsAgo = YearMonth.now().minusMonths(5).atDay(1).atStartOfDay();

        long totalPatients = userRepository.countByRole(Role.PATIENT);

        List<Appointment> completedThisMonth = appointmentRepository
                .findByStatusAndScheduledAtBetween(AppointmentStatus.COMPLETED, monthStart, now);
        BigDecimal totalRevenue = completedThisMonth.stream()
                .map(a -> a.getDoctor().getConsultationFee() != null ? a.getDoctor().getConsultationFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Appointment> completedSixMonths = appointmentRepository
                .findByStatusAndScheduledAtBetween(AppointmentStatus.COMPLETED, sixMonthsAgo, now);
        List<MonthlyRevenueDTO> monthlyRevenue = buildMonthlyRevenue(completedSixMonths, sixMonthsAgo);

        List<User> patientsSixMonths = userRepository
                .findByRoleAndCreatedAtBetween(Role.PATIENT, sixMonthsAgo, now);
        List<MonthlyPatientCountDTO> patientTrend = buildMonthlyPatientTrend(patientsSixMonths, sixMonthsAgo);

        return new AdminAnalyticsDTO(totalRevenue, totalPatients, monthlyRevenue, patientTrend);
    }

    private List<DailyStatsDTO> buildWeeklyStats(List<Appointment> appointments, List<User> patients, LocalDateTime todayStart) {
        List<DailyStatsDTO> result = new ArrayList<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = todayStart.minusDays(daysAgo).toLocalDate();
            String dayLabel = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            int patientCount = (int) patients.stream()
                    .filter(u -> u.getCreatedAt().toLocalDate().equals(date))
                    .count();

            List<Appointment> dayAppointments = appointments.stream()
                    .filter(a -> a.getScheduledAt().toLocalDate().equals(date))
                    .toList();

            BigDecimal revenue = dayAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .map(a -> a.getDoctor().getConsultationFee() != null ? a.getDoctor().getConsultationFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new DailyStatsDTO(dayLabel, patientCount, dayAppointments.size(), revenue));
        }
        return result;
    }

    private List<DepartmentLoadDTO> buildDepartmentLoad(List<Object[]> raw) {
        if (raw.isEmpty()) return List.of();
        long total = raw.stream().mapToLong(r -> (Long) r[1]).sum();
        if (total == 0) return List.of();
        return raw.stream()
                .map(r -> new DepartmentLoadDTO((String) r[0], (int) Math.round(((Long) r[1]) * 100.0 / total)))
                .toList();
    }

    private List<MonthlyRevenueDTO> buildMonthlyRevenue(List<Appointment> appointments, LocalDateTime from) {
        List<MonthlyRevenueDTO> result = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        YearMonth current = YearMonth.now();
        for (YearMonth ym = start; !ym.isAfter(current); ym = ym.plusMonths(1)) {
            final YearMonth month = ym;
            BigDecimal revenue = appointments.stream()
                    .filter(a -> YearMonth.from(a.getScheduledAt()).equals(month))
                    .map(a -> a.getDoctor().getConsultationFee() != null ? a.getDoctor().getConsultationFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new MonthlyRevenueDTO(label, revenue));
        }
        return result;
    }

    private List<MonthlyPatientCountDTO> buildMonthlyPatientTrend(List<User> patients, LocalDateTime from) {
        List<MonthlyPatientCountDTO> result = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        YearMonth current = YearMonth.now();
        for (YearMonth ym = start; !ym.isAfter(current); ym = ym.plusMonths(1)) {
            final YearMonth month = ym;
            int count = (int) patients.stream()
                    .filter(u -> YearMonth.from(u.getCreatedAt()).equals(month))
                    .count();
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new MonthlyPatientCountDTO(label, count));
        }
        return result;
    }

    @Override
    public List<AdminDepartmentDTO> getDepartments() {
        return specializationRepository.findAll().stream()
                .map(spec -> new AdminDepartmentDTO(
                        spec.getId(),
                        spec.getName(),
                        spec.getDescription(),
                        spec.getDoctors().size(),
                        (int) roomRepository.countByTypeAndSpecialization(RoomType.CONSULT, spec),
                        (int) roomRepository.countByTypeAndSpecialization(RoomType.OR, spec),
                        (int) roomRepository.countByTypeAndSpecialization(RoomType.IMAGING, spec)
                ))
                .toList();
    }

    @Override
    public List<RoomResponseDTO> getRooms() {
        Map<Long, DoctorProfile> byRoomId = doctorProfileRepository.findAll().stream()
                .filter(d -> d.getRoom() != null)
                .collect(Collectors.toMap(d -> d.getRoom().getId(), d -> d));
        return roomRepository.findAll().stream()
                .map(r -> roomMapper.toDto(r, byRoomId.get(r.getId())))
                .toList();
    }

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomUpdateDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ROOM_NOT_FOUND));
        if (dto.specializationId() != null) {
            room.setSpecialization(specializationRepository.findById(dto.specializationId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND)));
        } else {
            room.setSpecialization(null);
        }
        room.setType(dto.type());
        room.setStatus(dto.status());
        Room saved = roomRepository.save(room);
        DoctorProfile assignedDoctor = doctorProfileRepository.findByRoom(saved).orElse(null);
        return roomMapper.toDto(saved, assignedDoctor);
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO assignDoctorRoom(Long doctorProfileId, DoctorRoomAssignmentDTO dto) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND));

        if (dto.roomId() == null) {
            doctor.setRoom(null);
        } else {
            Room room = roomRepository.findById(dto.roomId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ROOM_NOT_FOUND));
            if (room.getType() != RoomType.CONSULT) {
                throw new BusinessException(ExceptionCode.ROOM_NOT_CONSULT_TYPE);
            }
            doctorProfileRepository.findByRoom(room).ifPresent(existing -> {
                if (!existing.getId().equals(doctorProfileId)) {
                    existing.setRoom(null);
                    doctorProfileRepository.save(existing);
                }
            });
            doctor.setRoom(room);
        }
        return doctorProfileMapper.toDto(doctorProfileRepository.save(doctor));
    }

    @Override
    public List<AdminPatientDTO> getAdminPatients() {
        return patientProfileRepository.findAll().stream()
                .map(p -> new AdminPatientDTO(
                        p.getId(),
                        p.getUser().getId(),
                        p.getUser().getFirstName(),
                        p.getUser().getLastName(),
                        p.getUser().getEmail(),
                        p.getUser().getPhoneNumber(),
                        p.getUser().isActive(),
                        p.getDateOfBirth(),
                        p.getGender(),
                        p.getBloodType(),
                        p.getAddress(),
                        p.getEmergencyContactName(),
                        p.getEmergencyContactPhone()
                ))
                .toList();
    }

    @Override
    public List<AppointmentResponseDTO> getPatientAppointments(Long patientProfileId) {
        if (!patientProfileRepository.existsById(patientProfileId)) {
            throw new DataNotFoundException(ExceptionCode.PATIENT_NOT_FOUND);
        }
        return appointmentRepository.findByPatient_IdOrderByScheduledAtDesc(patientProfileId)
                .stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    public List<AppointmentResponseDTO> getDoctorAppointments(Long doctorProfileId) {
        if (!doctorProfileRepository.existsById(doctorProfileId)) {
            throw new DataNotFoundException(ExceptionCode.DOCTOR_NOT_FOUND);
        }
        return appointmentRepository.findByDoctor_IdOrderByScheduledAtDesc(doctorProfileId)
                .stream().map(appointmentMapper::toDto).toList();
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserResponseDTO setActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
        user.setActive(active);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateDataException(ExceptionCode.EMAIL_ALREADY_EXISTS);
        }
        if (doctorProfileRepository.existsByLicenseNumber(dto.licenseNumber())) {
            throw new DuplicateDataException(ExceptionCode.LICENSE_NUMBER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .role(Role.DOCTOR)
                .active(true)
                .build();

        userRepository.save(user);

        List<Specialization> specializations = new ArrayList<>();
        if (dto.specializationIds() != null && !dto.specializationIds().isEmpty()) {
            specializations = dto.specializationIds().stream()
                    .map(id -> specializationRepository.findById(id)
                            .orElseThrow(() -> new DataNotFoundException(ExceptionCode.SPECIALIZATION_NOT_FOUND)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .licenseNumber(dto.licenseNumber())
                .biography(dto.biography())
                .specializations(specializations)
                .build();

        if (!specializations.isEmpty()) {
            roomRepository.findFreeConsultRoomsForSpecialization(
                            specializations.get(0), RoomType.CONSULT, RoomStatus.FREE)
                    .stream().findFirst()
                    .ifPresent(profile::setRoom);
        }

        return doctorProfileMapper.toDto(doctorProfileRepository.save(profile));
    }

    @Override
    public List<SpecializationResponseDTO> getAllSpecializations() {
        return specializationRepository.findAll().stream().map(specializationMapper::toDto).toList();
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_CANNOT_BE_MODIFIED);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }
}