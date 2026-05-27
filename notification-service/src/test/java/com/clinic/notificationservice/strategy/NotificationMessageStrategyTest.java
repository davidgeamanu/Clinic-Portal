package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMessageStrategyTest {

    private final NotificationContext context = new NotificationContext(
            LocalDateTime.of(2026, 6, 1, 10, 30),
            "John Doe",
            "Dr. Smith"
    );

    @Test
    void appointmentScheduled_containsPatientNameAndDate() {
        var strategy = new AppointmentScheduledMessage();
        assertEquals(NotificationType.APPOINTMENT_SCHEDULED, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("John Doe"));
        assertTrue(msg.contains("June 1, 2026"));
    }

    @Test
    void appointmentConfirmed_containsDate() {
        var strategy = new AppointmentConfirmedMessage();
        assertEquals(NotificationType.APPOINTMENT_CONFIRMED, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("confirmed"));
        assertTrue(msg.contains("June 1, 2026"));
    }

    @Test
    void appointmentStarted_containsDate() {
        var strategy = new AppointmentStartedMessage();
        assertEquals(NotificationType.APPOINTMENT_STARTED, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("started"));
    }

    @Test
    void appointmentCancelled_containsDate() {
        var strategy = new AppointmentCancelledMessage();
        assertEquals(NotificationType.APPOINTMENT_CANCELLED, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("cancelled"));
    }

    @Test
    void consultationNoteAdded_containsDate() {
        var strategy = new ConsultationNoteAddedMessage();
        assertEquals(NotificationType.CONSULTATION_NOTE_ADDED, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("consultation notes"));
    }

    @Test
    void general_fallback() {
        var strategy = new GeneralMessage();
        assertEquals(NotificationType.GENERAL, strategy.type());

        String msg = strategy.build(context);
        assertTrue(msg.contains("notification"));
    }

    @Test
    void registry_findsAllStrategies() {
        var registry = new NotificationMessageStrategyRegistry(java.util.List.of(
                new AppointmentScheduledMessage(),
                new AppointmentConfirmedMessage(),
                new AppointmentStartedMessage(),
                new AppointmentCancelledMessage(),
                new ConsultationNoteAddedMessage(),
                new GeneralMessage()
        ));

        for (NotificationType type : NotificationType.values()) {
            assertNotNull(registry.forType(type), "Missing strategy for " + type);
        }
    }
}
