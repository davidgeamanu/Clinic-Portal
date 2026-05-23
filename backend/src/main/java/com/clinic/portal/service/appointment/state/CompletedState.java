package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Terminal state for a finished consultation. Stamps {@code completedAt}
 * on entry so the duration becomes computable.
 */
@Component
public class CompletedState implements AppointmentState, EntryHook {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.COMPLETED;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of();
    }

    @Override
    public void onEntry(Appointment appointment) {
        appointment.setCompletedAt(LocalDateTime.now());
    }
}