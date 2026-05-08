package com.clinic.portal.mapper;

import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.Room;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.RoomType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toDto(Appointment appointment) {
        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();

        String roomNumber = null;
        RoomType roomType = null;
        String roomDepartment = null;
        if (appointment.getMode() == AppointmentMode.IN_PERSON) {
            Room room = appointment.getDoctor().getRoom();
            if (room != null) {
                roomNumber = room.getRoomNumber();
                roomType = room.getType();
                roomDepartment = room.getSpecialization() != null ? room.getSpecialization().getName() : null;
            }
        }

        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatient().getId(),
                patientUser.getFirstName() + " " + patientUser.getLastName(),
                appointment.getDoctor().getId(),
                doctorUser.getFirstName() + " " + doctorUser.getLastName(),
                appointment.getScheduledAt(),
                appointment.getDurationMinutes(),
                appointment.getMode(),
                roomNumber,
                roomType,
                roomDepartment,
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getCreatedAt()
        );
    }
}
