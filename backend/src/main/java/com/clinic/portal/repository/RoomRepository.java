package com.clinic.portal.repository;

import com.clinic.portal.model.Room;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByTypeAndSpecialization(RoomType type, Specialization specialization);

    boolean existsByRoomNumber(String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.type = :type AND r.status = :status AND r.specialization = :spec " +
           "AND NOT EXISTS (SELECT d FROM DoctorProfile d WHERE d.room = r)")
    List<Room> findFreeConsultRoomsForSpecialization(
            @Param("spec") Specialization spec,
            @Param("type") RoomType type,
            @Param("status") RoomStatus status
    );
}