package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Terminal state when an appointment is cancelled by either party or by
 * an administrator before it completes.
 */
@Component
public class CancelledState implements AppointmentState {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.CANCELLED;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of();
    }
}