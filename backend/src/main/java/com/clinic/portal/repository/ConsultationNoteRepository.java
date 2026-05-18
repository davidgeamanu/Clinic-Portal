package com.clinic.portal.repository;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    Optional<ConsultationNote> findByAppointment(Appointment appointment);

    List<ConsultationNote> findByAppointment_Patient_IdOrderByAppointment_ScheduledAtDesc(Long patientProfileId);

    List<ConsultationNote> findByAppointment_Doctor_User_IdOrderByAppointment_ScheduledAtDesc(Long doctorUserId);
}
