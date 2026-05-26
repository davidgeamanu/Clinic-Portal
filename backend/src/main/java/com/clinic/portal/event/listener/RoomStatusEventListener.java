package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.Room;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer that keeps a consultation room's occupancy in sync with the
 * lifecycle of the doctor's appointments.
 */
@Component
@RequiredArgsConstructor
public class RoomStatusEventListener {

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;

    @EventListener
    public void onAppointmentStatusChanged(AppointmentStatusChangedEvent event) {
        Appointment appointment = appointmentRepository.findById(event.appointmentId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getMode() != AppointmentMode.IN_PERSON) return;

        Room room = appointment.getDoctor().getRoom();
        if (room == null) return;

        boolean occupied = //event.newStatus() == AppointmentStatus.CONFIRMED
                 event.newStatus() == AppointmentStatus.IN_PROGRESS;
        room.setStatus(occupied ? RoomStatus.OCCUPIED : RoomStatus.FREE);
        roomRepository.save(room);
    }
}