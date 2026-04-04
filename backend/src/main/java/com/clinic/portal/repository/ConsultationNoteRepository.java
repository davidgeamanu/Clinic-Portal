package com.clinic.portal.repository;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    // Retrieve the note for a specific completed appointment
    Optional<ConsultationNote> findByAppointment(Appointment appointment);
}
