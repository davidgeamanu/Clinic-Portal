package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentStateTest {

    // --- ScheduledState ---

    @Nested
    class ScheduledStateTests {
        private final ScheduledState state = new ScheduledState();

        @Test
        void status_returnsScheduled() {
            assertEquals(AppointmentStatus.SCHEDULED, state.status());
        }

        @Test
        void allowedNextStates_containsConfirmedAndCancelled() {
            Set<AppointmentStatus> allowed = state.allowedNextStates();
            assertTrue(allowed.contains(AppointmentStatus.CONFIRMED));
            assertTrue(allowed.contains(AppointmentStatus.CANCELLED));
            assertEquals(2, allowed.size());
        }

        @Test
        void cannotGoDirectlyToNoShow_unconfirmedBookingsAreCancelledInstead() {
            assertFalse(state.allowedNextStates().contains(AppointmentStatus.NO_SHOW));
        }

        @Test
        void doesNotImplementEntryValidatorOrHook() {
            assertFalse(state instanceof EntryValidator);
            assertFalse(state instanceof EntryHook);
        }
    }

    // --- NoShowState ---

    @Nested
    class NoShowStateTests {
        private final NoShowState state = new NoShowState();

        @Test
        void status_returnsNoShow() {
            assertEquals(AppointmentStatus.NO_SHOW, state.status());
        }

        @Test
        void isTerminal_noNextStates() {
            assertTrue(state.allowedNextStates().isEmpty());
        }

        @Test
        void implementsEntryValidatorButNotHook() {
            assertInstanceOf(EntryValidator.class, state);
            assertFalse(state instanceof EntryHook);
        }

        @Test
        void validateEntry_passesOnceScheduledTimeHasPassed() {
            Appointment appointment = Appointment.builder()
                    .scheduledAt(LocalDateTime.now().minusMinutes(30))
                    .build();
            assertDoesNotThrow(() -> state.validateEntry(appointment));
        }

        @Test
        void validateEntry_throwsWhenAppointmentHasNotStartedYet() {
            Appointment appointment = Appointment.builder()
                    .scheduledAt(LocalDateTime.now().plusHours(2))
                    .build();
            assertThrows(Exception.class, () -> state.validateEntry(appointment));
        }
    }

    // --- ConfirmedState ---

    @Nested
    class ConfirmedStateTests {
        private final ConfirmedState state = new ConfirmedState();

        @Test
        void status_returnsConfirmed() {
            assertEquals(AppointmentStatus.CONFIRMED, state.status());
        }

        @Test
        void allowedNextStates_containsInProgressCancelledAndNoShow() {
            Set<AppointmentStatus> allowed = state.allowedNextStates();
            assertTrue(allowed.contains(AppointmentStatus.IN_PROGRESS));
            assertTrue(allowed.contains(AppointmentStatus.CANCELLED));
            assertTrue(allowed.contains(AppointmentStatus.NO_SHOW));
            assertEquals(3, allowed.size());
        }

        @Test
        void doesNotImplementEntryValidatorOrHook() {
            assertFalse(state instanceof EntryValidator);
            assertFalse(state instanceof EntryHook);
        }
    }

    // --- InProgressState ---

    @Nested
    class InProgressStateTests {
        private final InProgressState state = new InProgressState();

        @Test
        void status_returnsInProgress() {
            assertEquals(AppointmentStatus.IN_PROGRESS, state.status());
        }

        @Test
        void allowedNextStates_containsOnlyCompleted() {
            Set<AppointmentStatus> allowed = state.allowedNextStates();
            assertTrue(allowed.contains(AppointmentStatus.COMPLETED));
            assertEquals(1, allowed.size());
        }

        @Test
        void implementsBothEntryValidatorAndHook() {
            assertInstanceOf(EntryValidator.class, state);
            assertInstanceOf(EntryHook.class, state);
        }

        @Test
        void validateEntry_passesWhenScheduledAtIsNowOrPast() {
            Appointment appointment = Appointment.builder()
                    .scheduledAt(LocalDateTime.now().minusMinutes(5))
                    .build();
            assertDoesNotThrow(() -> state.validateEntry(appointment));
        }

        @Test
        void validateEntry_throwsWhenScheduledAtIsFuture() {
            Appointment appointment = Appointment.builder()
                    .scheduledAt(LocalDateTime.now().plusHours(1))
                    .build();
            assertThrows(Exception.class, () -> state.validateEntry(appointment));
        }

        @Test
        void onEntry_setsStartedAt() {
            Appointment appointment = Appointment.builder()
                    .scheduledAt(LocalDateTime.now().minusMinutes(5))
                    .build();
            assertNull(appointment.getStartedAt());

            state.onEntry(appointment);

            assertNotNull(appointment.getStartedAt());
        }
    }

    // --- CompletedState ---

    @Nested
    class CompletedStateTests {
        private final CompletedState state = new CompletedState();

        @Test
        void status_returnsCompleted() {
            assertEquals(AppointmentStatus.COMPLETED, state.status());
        }

        @Test
        void allowedNextStates_isEmpty() {
            assertTrue(state.allowedNextStates().isEmpty());
        }

        @Test
        void implementsEntryHookButNotValidator() {
            assertInstanceOf(EntryHook.class, state);
            assertFalse(state instanceof EntryValidator);
        }

        @Test
        void onEntry_setsCompletedAt() {
            Appointment appointment = Appointment.builder().build();
            assertNull(appointment.getCompletedAt());

            state.onEntry(appointment);

            assertNotNull(appointment.getCompletedAt());
        }
    }

    // --- CancelledState ---

    @Nested
    class CancelledStateTests {
        private final CancelledState state = new CancelledState();

        @Test
        void status_returnsCancelled() {
            assertEquals(AppointmentStatus.CANCELLED, state.status());
        }

        @Test
        void allowedNextStates_isEmpty() {
            assertTrue(state.allowedNextStates().isEmpty());
        }

        @Test
        void doesNotImplementEntryValidatorOrHook() {
            assertFalse(state instanceof EntryValidator);
            assertFalse(state instanceof EntryHook);
        }
    }
}
