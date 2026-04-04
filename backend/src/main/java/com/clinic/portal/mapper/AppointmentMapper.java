package com.clinic.portal.mapper;

import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.User;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toDto(Appointment appointment) {
        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();

        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatient().getId(),
                patientUser.getFirstName() + " " + patientUser.getLastName(),
                appointment.getDoctor().getId(),
                doctorUser.getFirstName() + " " + doctorUser.getLastName(),
                appointment.getScheduledAt(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getCreatedAt()
        );
    }
}
