package com.clinic.portal.mapper;

import com.clinic.portal.dto.admin.RoomResponseDTO;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponseDTO toDto(Room room, DoctorProfile assignedDoctor) {
        return new RoomResponseDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getType(),
                room.getStatus(),
                room.getSpecialization() != null ? room.getSpecialization().getId() : null,
                room.getSpecialization() != null ? room.getSpecialization().getName() : null,
                assignedDoctor != null ? assignedDoctor.getId() : null,
                assignedDoctor != null
                        ? assignedDoctor.getUser().getFirstName() + " " + assignedDoctor.getUser().getLastName()
                        : null
        );
    }

    public RoomResponseDTO toDto(Room room) {
        return toDto(room, null);
    }
}