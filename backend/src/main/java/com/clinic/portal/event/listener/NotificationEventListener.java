package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.event.ConsultationNoteCreatedEvent;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.ConsultationNote;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.NotificationType;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.service.NotificationService;
import com.clinic.portal.service.notification.message.NotificationMessageStrategy;
import com.clinic.portal.service.notification.message.NotificationMessageStrategyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer responsible for dispatching user-facing notifications in response to
 * domain events.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final NotificationService notificationService;
    private final NotificationMessageStrategyRegistry messageRegistry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());
        User doctorUser = appointment.getDoctor().getUser();

        dispatch(doctorUser, NotificationType.APPOINTMENT_SCHEDULED, appointment, appointment.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAppointmentStatusChanged(AppointmentStatusChangedEvent event) {
        Appointment appointment = findAppointment(event.appointmentId());

        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();
        User recipient = event.actorUserId().equals(patientUser.getId()) ? doctorUser : patientUser;

        dispatch(recipient, notificationTypeFor(event.newStatus()), appointment, appointment.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onConsultationNoteCreated(ConsultationNoteCreatedEvent event) {
        ConsultationNote note = consultationNoteRepository.findById(event.noteId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.CONSULTATION_NOTE_NOT_FOUND));
        Appointment appointment = note.getAppointment();

        dispatch(appointment.getPatient().getUser(), NotificationType.CONSULTATION_NOTE_ADDED, appointment, note.getId());
    }

    private void dispatch(User recipient, NotificationType type, Appointment appointment, Long relatedEntityId) {
        NotificationMessageStrategy strategy = messageRegistry.forType(type);
        notificationService.send(recipient, strategy.build(appointment), type, relatedEntityId);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));
    }

    private NotificationType notificationTypeFor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED   -> NotificationType.APPOINTMENT_CONFIRMED;
            case IN_PROGRESS -> NotificationType.APPOINTMENT_STARTED;
            case CANCELLED   -> NotificationType.APPOINTMENT_CANCELLED;
            default          -> NotificationType.GENERAL;
        };
    }
}