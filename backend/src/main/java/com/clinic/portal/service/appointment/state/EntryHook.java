package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.Appointment;

/**
 * Opt-in capability for {@link AppointmentState}s that need to apply
 * entry-time side effects to the {@link Appointment} once it has been
 * transitioned into the state.
 */
public interface EntryHook {

    void onEntry(Appointment appointment);
}