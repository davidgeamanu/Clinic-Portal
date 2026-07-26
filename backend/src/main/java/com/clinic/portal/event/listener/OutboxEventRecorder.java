package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.event.ConsultationNoteCreatedEvent;
import com.clinic.portal.event.messaging.AppointmentBookedMessage;
import com.clinic.portal.event.messaging.AppointmentStatusChangedMessage;
import com.clinic.portal.event.messaging.ConsultationNoteCreatedMessage;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.OutboxEvent;
import com.clinic.portal.model.User;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Bridges internal Spring domain events to the transactional outbox by
 * enriching the thin in-process events with names and emails, then writing
 * the fat event as an outbox row IN THE SAME TRANSACTION as the domain
 * change (BEFORE_COMMIT). The {@code OutboxRelay} does the actual RabbitMQ
 * publishing asynchronously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRecorder {

    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.messaging.appointment-booked-queue}")
    private String appointmentBookedQueue;

    @Value("${app.messaging.appointment-status-changed-queue}")
    private String appointmentStatusChangedQueue;

    @Value("${app.messaging.consultation-note-created-queue}")
    private String consultationNoteCreatedQueue;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        UUID eventId = UUID.randomUUID();
        var fatEvent = new AppointmentBookedMessage(
                eventId,
                appointment.getId(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor), doctor.getEmail(),
                appointment.getScheduledAt()
        );

        record(eventId, fatEvent.getClass().getSimpleName(), appointmentBookedQueue, fatEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAppointmentStatusChanged(AppointmentStatusChangedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        UUID eventId = UUID.randomUUID();
        var fatEvent = new AppointmentStatusChangedMessage(
                eventId,
                appointment.getId(),
                event.previousStatus(),
                event.newStatus(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor), doctor.getEmail(),
                event.actorUserId(),
                appointment.getScheduledAt()
        );

        record(eventId, fatEvent.getClass().getSimpleName(), appointmentStatusChangedQueue, fatEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onConsultationNoteCreated(ConsultationNoteCreatedEvent event) {
        ConsultationNote note = consultationNoteRepository.findById(event.noteId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));
        Appointment appointment = note.getAppointment();
        User patient = appointment.getPatient().getUser();
        User doctor = appointment.getDoctor().getUser();

        UUID eventId = UUID.randomUUID();
        var fatEvent = new ConsultationNoteCreatedMessage(
                eventId,
                note.getId(),
                appointment.getId(),
                patient.getId(), fullName(patient), patient.getEmail(),
                doctor.getId(), fullName(doctor),
                appointment.getScheduledAt()
        );

        record(eventId, fatEvent.getClass().getSimpleName(), consultationNoteCreatedQueue, fatEvent);
    }

    private void record(UUID eventId, String eventType, String queue, Object fatEvent) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .queue(queue)
                .payload(objectMapper.writeValueAsString(fatEvent))
                .correlationId(MDC.get("correlationId"))
                .build();

        outboxEventRepository.save(outboxEvent);
        log.debug("Recorded outbox event {} ({}) for queue {}", eventId, eventType, queue);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
