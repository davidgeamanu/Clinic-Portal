package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initial state assigned when a patient books an appointment.
 * Awaiting doctor confirmation.
 *
 * A booking still unconfirmed at its start time is auto-cancelled by the
 * expiration sweep; it never becomes NO_SHOW, which is reserved for confirmed
 * appointments the patient did not attend.
 */
@Component
public class ScheduledState implements AppointmentState {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.SCHEDULED;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED);
    }
}