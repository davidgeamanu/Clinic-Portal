package com.clinic.notificationservice.consumer;

import com.clinic.notificationservice.event.AppointmentBookedEvent;
import com.clinic.notificationservice.event.AppointmentStatus;
import com.clinic.notificationservice.event.AppointmentStatusChangedEvent;
import com.clinic.notificationservice.event.ConsultationNoteCreatedEvent;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.service.NotificationService;
import com.clinic.notificationservice.strategy.NotificationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock private NotificationService notificationService;
    @InjectMocks private NotificationEventConsumer consumer;

    private final LocalDateTime scheduledAt = LocalDateTime.of(2026, 6, 1, 10, 0);

    @Test
    void onBooked_dispatchesToDoctor() {
        var event = new AppointmentBookedEvent(
                UUID.randomUUID(), 1L,
                10L, "John Patient", "john@test.com",
                20L, "Dr Smith", "smith@test.com",
                scheduledAt
        );

        consumer.onBooked(event);

        verify(notificationService).dispatch(
                eq(20L), eq("smith@test.com"), eq("Dr Smith"),
                eq(NotificationType.APPOINTMENT_SCHEDULED),
                any(NotificationContext.class), eq(1L)
        );
    }

    @Test
    void onStatusChanged_confirmed_dispatchesToPatient() {
        var event = new AppointmentStatusChangedEvent(
                UUID.randomUUID(), 1L,
                AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED,
                10L, "John", "john@test.com",
                20L, "Dr Smith", "smith@test.com",
                20L, scheduledAt
        );

        consumer.onStatusChanged(event);

        verify(notificationService).dispatch(
                eq(10L), eq("john@test.com"), eq("John"),
                eq(NotificationType.APPOINTMENT_CONFIRMED),
                any(), eq(1L)
        );
    }

    @Test
    void onStatusChanged_cancelledByPatient_dispatchesToDoctor() {
        var event = new AppointmentStatusChangedEvent(
                UUID.randomUUID(), 1L,
                AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED,
                10L, "John", "john@test.com",
                20L, "Dr Smith", "smith@test.com",
                10L, scheduledAt  // patient is the requester
        );

        consumer.onStatusChanged(event);

        verify(notificationService).dispatch(
                eq(20L), eq("smith@test.com"), eq("Dr Smith"),
                eq(NotificationType.APPOINTMENT_CANCELLED),
                any(), eq(1L)
        );
    }

    @Test
    void onStatusChanged_cancelledByDoctor_dispatchesToPatient() {
        var event = new AppointmentStatusChangedEvent(
                UUID.randomUUID(), 1L,
                AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED,
                10L, "John", "john@test.com",
                20L, "Dr Smith", "smith@test.com",
                20L, scheduledAt  // doctor is the requester
        );

        consumer.onStatusChanged(event);

        verify(notificationService).dispatch(
                eq(10L), eq("john@test.com"), eq("John"),
                eq(NotificationType.APPOINTMENT_CANCELLED),
                any(), eq(1L)
        );
    }

    @Test
    void onStatusChanged_completed_doesNotDispatch() {
        var event = new AppointmentStatusChangedEvent(
                UUID.randomUUID(), 1L,
                AppointmentStatus.IN_PROGRESS, AppointmentStatus.COMPLETED,
                10L, "John", "john@test.com",
                20L, "Dr Smith", "smith@test.com",
                20L, scheduledAt
        );

        consumer.onStatusChanged(event);

        verifyNoInteractions(notificationService);
    }

    @Test
    void onNoteCreated_dispatchesToPatient() {
        var event = new ConsultationNoteCreatedEvent(
                UUID.randomUUID(), 50L, 1L,
                10L, "John", "john@test.com",
                20L, "Dr Smith",
                scheduledAt
        );

        consumer.onNoteCreated(event);

        verify(notificationService).dispatch(
                eq(10L), eq("john@test.com"), eq("John"),
                eq(NotificationType.CONSULTATION_NOTE_ADDED),
                any(), eq(50L)
        );
    }
}
