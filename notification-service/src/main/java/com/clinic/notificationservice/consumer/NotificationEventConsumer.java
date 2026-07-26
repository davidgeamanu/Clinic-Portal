package com.clinic.notificationservice.consumer;

import com.clinic.notificationservice.event.AppointmentBookedEvent;
import com.clinic.notificationservice.event.AppointmentStatus;
import com.clinic.notificationservice.event.AppointmentStatusChangedEvent;
import com.clinic.notificationservice.event.ConsultationNoteCreatedEvent;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.model.ProcessedEvent;
import com.clinic.notificationservice.repository.ProcessedEventRepository;
import com.clinic.notificationservice.service.NotificationService;
import com.clinic.notificationservice.strategy.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Consumes fat events from RabbitMQ and turns them into notifications.
 *
 * Queues are declared with a dead-letter exchange: after the configured retry
 * attempts are exhausted, the poison message lands in the DLQ for inspection
 * instead of being redelivered forever.
 *
 * Processing is idempotent — each event's {@code eventId} is recorded in the
 * same transaction as the notification, so redeliveries are skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    static final String CORRELATION_ID_HEADER = "x-correlation-id";

    private final NotificationService notificationService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.appointment-booked-queue}", durable = "true",
            arguments = {
                    @Argument(name = "x-dead-letter-exchange", value = ""),
                    @Argument(name = "x-dead-letter-routing-key", value = "${app.messaging.dead-letter-queue}")
            }))
    @Transactional
    public void onBooked(AppointmentBookedEvent event,
                         @Header(name = CORRELATION_ID_HEADER, required = false) String correlationId) {
        withCorrelation(correlationId, () -> {
            log.info("Received AppointmentBookedEvent: appointmentId={}", event.appointmentId());
            if (alreadyProcessed(event.eventId())) return;

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
            markProcessed(event.eventId(), "AppointmentBookedEvent");
        });
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.appointment-status-changed-queue}", durable = "true",
            arguments = {
                    @Argument(name = "x-dead-letter-exchange", value = ""),
                    @Argument(name = "x-dead-letter-routing-key", value = "${app.messaging.dead-letter-queue}")
            }))
    @Transactional
    public void onStatusChanged(AppointmentStatusChangedEvent event,
                                @Header(name = CORRELATION_ID_HEADER, required = false) String correlationId) {
        withCorrelation(correlationId, () -> {
            log.info("Received AppointmentStatusChangedEvent: appointmentId={}, {} -> {}",
                    event.appointmentId(), event.previousStatus(), event.newStatus());

            NotificationType type = notificationTypeFor(event.newStatus());
            if (type == null) return;
            if (alreadyProcessed(event.eventId())) return;

            Long recipientUserId;
            String recipientEmail;
            String recipientName;

            // NO_SHOW always notifies the patient (they missed it); CANCELLED
            // notifies whichever party did not initiate the cancellation.
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
            markProcessed(event.eventId(), "AppointmentStatusChangedEvent");
        });
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "${app.messaging.consultation-note-created-queue}", durable = "true",
            arguments = {
                    @Argument(name = "x-dead-letter-exchange", value = ""),
                    @Argument(name = "x-dead-letter-routing-key", value = "${app.messaging.dead-letter-queue}")
            }))
    @Transactional
    public void onNoteCreated(ConsultationNoteCreatedEvent event,
                              @Header(name = CORRELATION_ID_HEADER, required = false) String correlationId) {
        withCorrelation(correlationId, () -> {
            log.info("Received ConsultationNoteCreatedEvent: noteId={}", event.noteId());
            if (alreadyProcessed(event.eventId())) return;

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
            markProcessed(event.eventId(), "ConsultationNoteCreatedEvent");
        });
    }

    private boolean alreadyProcessed(UUID eventId) {
        if (eventId == null) return false;
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("Skipping duplicate event {} (already processed)", eventId);
            return true;
        }
        return false;
    }

    private void markProcessed(UUID eventId, String eventType) {
        if (eventId == null) return;
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .build());
    }

    private void withCorrelation(String correlationId, Runnable action) {
        if (correlationId == null) {
            action.run();
            return;
        }
        MDC.put("correlationId", correlationId);
        try {
            action.run();
        } finally {
            MDC.remove("correlationId");
        }
    }

    private NotificationType notificationTypeFor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.APPOINTMENT_CONFIRMED;
            case IN_PROGRESS -> NotificationType.APPOINTMENT_STARTED;
            case CANCELLED -> NotificationType.APPOINTMENT_CANCELLED;
            case NO_SHOW -> NotificationType.APPOINTMENT_NO_SHOW;
            default -> null;
        };
    }
}
