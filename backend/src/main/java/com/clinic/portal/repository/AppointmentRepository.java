package com.clinic.portal.repository;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.projection.StaleAppointmentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientOrderByScheduledAtDesc(PatientProfile patient);

    List<Appointment> findByPatientAndStatus(PatientProfile patient, AppointmentStatus status);

    List<Appointment> findByDoctorOrderByScheduledAtAsc(DoctorProfile doctor);

    List<Appointment> findByDoctorOrderByScheduledAtDesc(DoctorProfile doctor);

    List<Appointment> findByDoctorAndStatus(DoctorProfile doctor, AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.id = :id AND (a.patient.id = :profileId OR a.doctor.id = :profileId)")
    long countByIdAndProfileId(@Param("id") Long id, @Param("profileId") Long profileId);

    long countByScheduledAtBetween(LocalDateTime from, LocalDateTime to);

    List<Appointment> findByScheduledAtBetween(LocalDateTime from, LocalDateTime to);

    List<Appointment> findByStatusAndScheduledAtBetween(AppointmentStatus status, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT a.id AS id, d.user.id AS doctorUserId
            FROM Appointment a JOIN a.doctor d
            WHERE a.status = :status AND a.scheduledAt < :cutoff
            """)
    List<StaleAppointmentView> findStaleForCancellation(
            @Param("status") AppointmentStatus status,
            @Param("cutoff") LocalDateTime cutoff);

    List<Appointment> findByPatient_IdOrderByScheduledAtDesc(Long patientProfileId);

    List<Appointment> findByDoctor_IdOrderByScheduledAtDesc(Long doctorProfileId);

    List<Appointment> findByDoctorAndStatusOrderByScheduledAtDesc(DoctorProfile doctor, AppointmentStatus status);

    List<Appointment> findByDoctor_IdAndScheduledAtBetweenAndStatusNot(
            Long doctorId, LocalDateTime from, LocalDateTime to, AppointmentStatus excludedStatus);

    boolean existsByDoctorAndPatient(DoctorProfile doctor, PatientProfile patient);

    @Query("SELECT s.name, COUNT(a) FROM Appointment a JOIN a.doctor.specializations s GROUP BY s.name ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsBySpecialization();

    @Query("SELECT a FROM Appointment a WHERE a.status = com.clinic.portal.model.enums.AppointmentStatus.COMPLETED AND a.startedAt IS NOT NULL AND a.completedAt IS NOT NULL")
    List<Appointment> findCompletedWithTimestamps();
}
