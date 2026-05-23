package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * The appointment has been confirmed by the doctor and is awaiting the
 * scheduled start time.
 */
@Component
public class ConfirmedState implements AppointmentState {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.CONFIRMED;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED);
    }
}