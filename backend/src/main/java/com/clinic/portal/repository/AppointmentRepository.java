package com.clinic.portal.repository;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Patient dashboard: all appointments for a patient, newest first
    List<Appointment> findByPatientOrderByScheduledAtDesc(PatientProfile patient);

    // Patient dashboard: filter by status (e.g. show only upcoming SCHEDULED ones)
    List<Appointment> findByPatientAndStatus(PatientProfile patient, AppointmentStatus status);

    // Doctor dashboard: today's and upcoming schedule
    List<Appointment> findByDoctorOrderByScheduledAtAsc(DoctorProfile doctor);

    // Doctor dashboard: filter by status (e.g. only CONFIRMED appointments)
    List<Appointment> findByDoctorAndStatus(DoctorProfile doctor, AppointmentStatus status);

    // Conflict check: does the doctor already have a booking in this time window?
    // Used before confirming a new appointment to prevent double-booking.
    boolean existsByDoctorAndScheduledAtBetween(
            DoctorProfile doctor,
            LocalDateTime from,
            LocalDateTime to
    );

    // Authorization check: does this appointment belong to the given patient or doctor profile?
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.id = :id AND (a.patient.id = :profileId OR a.doctor.id = :profileId)")
    long countByIdAndProfileId(@Param("id") Long id, @Param("profileId") Long profileId);
}
