package com.clinic.portal.service.appointment.state;

import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * The consultation has started. Entry is rejected if the scheduled time
 * is still in the future.
 */
@Component
public class InProgressState implements AppointmentState, EntryValidator, EntryHook {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.IN_PROGRESS;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of(AppointmentStatus.COMPLETED);
    }

    @Override
    public void validateEntry(Appointment appointment) {
        long minutesUntilStart = ChronoUnit.MINUTES.between(LocalDateTime.now(), appointment.getScheduledAt());
        if (minutesUntilStart > 0) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_NOT_YET_STARTABLE);
        }
    }

    @Override
    public void onEntry(Appointment appointment) {
        appointment.setStartedAt(LocalDateTime.now());
    }
}