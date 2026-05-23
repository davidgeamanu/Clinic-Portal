package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.Appointment;

/**
 * Opt-in capability for {@link AppointmentState}s that need to enforce a
 * precondition before they may be entered.
 */
public interface EntryValidator {

    /**
     * Invoked before the status mutation. Implementations throw a
     * {@code BusinessException} if the appointment is not yet eligible to
     * enter this state
     */
    void validateEntry(Appointment appointment);
}