package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.event.ConsultationNoteCreatedEvent;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.User;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Bridges internal Spring domain events to RabbitMQ by enriching the thin
 * in-process events with names and emails, then publishing fat events.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;

    @Value("${app.messaging.appointment-booked-queue}")
    private String appointmentBookedQueue;

    @Value("${app.messaging.appointment-status-changed-queue}")
    private String appointmentStatusChangedQueue;

    @Value("${app.messaging.consultation-note-created-queue}")
    private String consultationNoteCreatedQueue;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        var fatEvent = new com.clinic.portal.event.messaging.AppointmentBookedEvent(
                UUID.randomUUID(),
                appointment.getId(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor), doctor.getEmail(),
                appointment.getScheduledAt()
        );

        rabbitTemplate.convertAndSend(appointmentBookedQueue, fatEvent);
        log.info("Published AppointmentBookedEvent to RabbitMQ: appointmentId={}", appointment.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAppointmentStatusChanged(AppointmentStatusChangedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        var fatEvent = new com.clinic.portal.event.messaging.AppointmentStatusChangedEvent(
                UUID.randomUUID(),
                appointment.getId(),
                event.previousStatus(),
                event.newStatus(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor), doctor.getEmail(),
                event.actorUserId(),
                appointment.getScheduledAt()
        );

        rabbitTemplate.convertAndSend(appointmentStatusChangedQueue, fatEvent);
        log.info("Published AppointmentStatusChangedEvent to RabbitMQ: appointmentId={}, {} -> {}",
                appointment.getId(), event.previousStatus(), event.newStatus());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onConsultationNoteCreated(ConsultationNoteCreatedEvent event) {
        ConsultationNote note = consultationNoteRepository.findById(event.noteId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));
        Appointment appointment = note.getAppointment();
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        var fatEvent = new com.clinic.portal.event.messaging.ConsultationNoteCreatedEvent(
                UUID.randomUUID(),
                note.getId(),
                appointment.getId(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor),
                appointment.getScheduledAt()
        );

        rabbitTemplate.convertAndSend(consultationNoteCreatedQueue, fatEvent);
        log.info("Published ConsultationNoteCreatedEvent to RabbitMQ: noteId={}", note.getId());
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
