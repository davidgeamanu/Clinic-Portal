package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentStateRegistryTest {

    private AppointmentStateRegistry registry;

    @BeforeEach
    void setUp() {
        List<AppointmentState> states = List.of(
                new ScheduledState(),
                new ConfirmedState(),
                new InProgressState(),
                new CompletedState(),
                new CancelledState(),
                new NoShowState()
        );
        registry = new AppointmentStateRegistry(states);
    }

    @Test
    void forStatus_returnsCorrectStateForEachStatus() {
        for (AppointmentStatus status : AppointmentStatus.values()) {
            AppointmentState state = registry.forStatus(status);
            assertNotNull(state, "No state registered for " + status);
            assertEquals(status, state.status());
        }
    }

    @Test
    void forStatus_scheduledReturnsScheduledState() {
        assertInstanceOf(ScheduledState.class, registry.forStatus(AppointmentStatus.SCHEDULED));
    }

    @Test
    void forStatus_inProgressReturnsInProgressState() {
        AppointmentState state = registry.forStatus(AppointmentStatus.IN_PROGRESS);
        assertInstanceOf(InProgressState.class, state);
        assertInstanceOf(EntryValidator.class, state);
        assertInstanceOf(EntryHook.class, state);
    }

    @Test
    void forStatus_completedReturnsCompletedState() {
        AppointmentState state = registry.forStatus(AppointmentStatus.COMPLETED);
        assertInstanceOf(CompletedState.class, state);
        assertInstanceOf(EntryHook.class, state);
    }
}
