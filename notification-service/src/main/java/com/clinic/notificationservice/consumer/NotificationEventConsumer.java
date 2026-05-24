package com.clinic.notificationservice.consumer;

import com.clinic.notificationservice.event.AppointmentBookedEvent;
import com.clinic.notificationservice.event.AppointmentStatus;
import com.clinic.notificationservice.event.AppointmentStatusChangedEvent;
import com.clinic.notificationservice.event.ConsultationNoteCreatedEvent;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.service.NotificationService;
import com.clinic.notificationservice.strategy.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.appointment-booked-queue}", durable = "true"))
    public void onBooked(AppointmentBookedEvent event) {
        log.info("Received AppointmentBookedEvent: appointmentId={}", event.appointmentId());

        NotificationContext ctx = new NotificationContext(
                event.scheduledAt(), event.patientName(), event.doctorName());

        notificationService.dispatch(
                event.doctorUserId(),
                event.doctorEmail(),
                event.doctorName(),
                NotificationType.APPOINTMENT_SCHEDULED,
                ctx,
                event.appointmentId()
        );
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.appointment-status-changed-queue}", durable = "true"))
    public void onStatusChanged(AppointmentStatusChangedEvent event) {
        log.info("Received AppointmentStatusChangedEvent: appointmentId={}, {} -> {}",
                event.appointmentId(), event.previousStatus(), event.newStatus());

        NotificationType type = notificationTypeFor(event.newStatus());
        if (type == null) return;

        Long recipientUserId;
        String recipientEmail;
        String recipientName;

        if (event.newStatus() == AppointmentStatus.CANCELLED) {
            boolean patientInitiated = event.requestingUserId().equals(event.patientUserId());
            recipientUserId = patientInitiated ? event.doctorUserId() : event.patientUserId();
            recipientEmail = patientInitiated ? event.doctorEmail() : event.patientEmail();
            recipientName = patientInitiated ? event.doctorName() : event.patientName();
        } else {
            recipientUserId = event.patientUserId();
            recipientEmail = event.patientEmail();
            recipientName = event.patientName();
        }

        NotificationContext ctx = new NotificationContext(
                event.scheduledAt(), event.patientName(), event.doctorName());

        notificationService.dispatch(recipientUserId, recipientEmail, recipientName, type, ctx, event.appointmentId());
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.consultation-note-created-queue}", durable = "true"))
    public void onNoteCreated(ConsultationNoteCreatedEvent event) {
        log.info("Received ConsultationNoteCreatedEvent: noteId={}", event.noteId());

        NotificationContext ctx = new NotificationContext(
                event.scheduledAt(), event.patientName(), event.doctorName());

        notificationService.dispatch(
                event.patientUserId(),
                event.patientEmail(),
                event.patientName(),
                NotificationType.CONSULTATION_NOTE_ADDED,
                ctx,
                event.noteId()
        );
    }

    private NotificationType notificationTypeFor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.APPOINTMENT_CONFIRMED;
            case IN_PROGRESS -> NotificationType.APPOINTMENT_STARTED;
            case CANCELLED -> NotificationType.APPOINTMENT_CANCELLED;
            default -> null;
        };
    }
}
