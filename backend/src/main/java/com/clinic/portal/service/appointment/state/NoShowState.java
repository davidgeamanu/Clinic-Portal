package com.clinic.portal.service.appointment.state;

import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Terminal state for a confirmed appointment the patient did not attend.
 *
 * Only a doctor or an admin can enter this state, and only once the scheduled
 * start time has passed — an appointment that has not happened yet cannot have
 * been missed. Unconfirmed bookings that expire are cancelled instead.
 */
@Component
public class NoShowState implements AppointmentState, EntryValidator {

    @Override
    public AppointmentStatus status() {
        return AppointmentStatus.NO_SHOW;
    }

    @Override
    public Set<AppointmentStatus> allowedNextStates() {
        return Set.of();
    }

    @Override
    public void validateEntry(Appointment appointment) {
        if (appointment.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ExceptionCode.APPOINTMENT_NOT_YET_MISSED);
        }
    }
}
