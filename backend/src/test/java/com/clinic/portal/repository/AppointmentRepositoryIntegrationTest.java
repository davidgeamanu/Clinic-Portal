package com.clinic.portal.repository;

import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryIntegrationTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private EntityManager em;

    private PatientProfile patient;
    private DoctorProfile doctor;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder()
                .email("patient@test.com").passwordHash("hash")
                .firstName("John").lastName("Doe")
                .role(Role.PATIENT).build();
        em.persist(patientUser);

        patient = new PatientProfile();
        patient.setUser(patientUser);
        em.persist(patient);

        User doctorUser = User.builder()
                .email("doctor@test.com").passwordHash("hash")
                .firstName("Dr").lastName("Smith")
                .role(Role.DOCTOR).build();
        em.persist(doctorUser);

        doctor = new DoctorProfile();
        doctor.setUser(doctorUser);
        doctor.setLicenseNumber("LIC-001");
        em.persist(doctor);

        em.flush();
    }

    @Test
    void findByPatientOrderByScheduledAtDesc_returnsInDescOrder() {
        Appointment a1 = createAppointment(LocalDateTime.of(2026, 6, 1, 9, 0));
        Appointment a2 = createAppointment(LocalDateTime.of(2026, 6, 2, 10, 0));
        Appointment a3 = createAppointment(LocalDateTime.of(2026, 6, 1, 14, 0));

        List<Appointment> result = appointmentRepository.findByPatientOrderByScheduledAtDesc(patient);

        assertEquals(3, result.size());
        assertTrue(result.get(0).getScheduledAt().isAfter(result.get(1).getScheduledAt()));
        assertTrue(result.get(1).getScheduledAt().isAfter(result.get(2).getScheduledAt()));
    }

    @Test
    void findByDoctorAndStatus_filtersCorrectly() {
        Appointment scheduled = createAppointment(LocalDateTime.of(2026, 6, 1, 9, 0));
        Appointment cancelled = createAppointment(LocalDateTime.of(2026, 6, 2, 10, 0));
        cancelled.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(cancelled);

        List<Appointment> result = appointmentRepository.findByDoctorAndStatus(doctor, AppointmentStatus.SCHEDULED);

        assertEquals(1, result.size());
        assertEquals(scheduled.getId(), result.getFirst().getId());
    }

    @Test
    void countByIdAndProfileId_matchesPatient() {
        Appointment a = createAppointment(LocalDateTime.of(2026, 6, 1, 9, 0));

        long count = appointmentRepository.countByIdAndProfileId(a.getId(), patient.getId());
        assertEquals(1, count);
    }

    @Test
    void countByIdAndProfileId_returnsZeroForUnrelatedProfile() {
        Appointment a = createAppointment(LocalDateTime.of(2026, 6, 1, 9, 0));

        long count = appointmentRepository.countByIdAndProfileId(a.getId(), 9999L);
        assertEquals(0, count);
    }

    @Test
    void countByScheduledAtBetween_countsCorrectRange() {
        createAppointment(LocalDateTime.of(2026, 6, 1, 9, 0));
        createAppointment(LocalDateTime.of(2026, 6, 1, 14, 0));
        createAppointment(LocalDateTime.of(2026, 6, 3, 10, 0));

        long count = appointmentRepository.countByScheduledAtBetween(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59));

        assertEquals(2, count);
    }

    private Appointment createAppointment(LocalDateTime scheduledAt) {
        Appointment a = Appointment.builder()
                .patient(patient).doctor(doctor)
                .scheduledAt(scheduledAt).durationMinutes(30)
                .mode(AppointmentMode.IN_PERSON)
                .build();
        return appointmentRepository.save(a);
    }
}
