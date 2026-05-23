package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;

import java.util.Set;

/**
 * State role in the State pattern for the appointment lifecycle.
 */
public interface AppointmentState {

    AppointmentStatus status();

    Set<AppointmentStatus> allowedNextStates();
}