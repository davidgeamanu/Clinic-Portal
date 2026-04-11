package com.clinic.portal.controller.appointment;

import com.clinic.portal.dto.appointment.AppointmentRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.security.UserDetailsImpl;
import com.clinic.portal.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentControllerImpl implements AppointmentController {

    private final AppointmentService appointmentService;

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentResponseDTO book(AppointmentRequestDTO dto) {
        log.info("[APPOINTMENT] Booking appointment for user: {}", currentUser().getId());
        return appointmentService.bookAppointment(currentUser().getId(), dto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnAppointment(#appointmentId)")
    public AppointmentResponseDTO getById(Long appointmentId) {
        log.info("[APPOINTMENT] Getting appointment: {}", appointmentId);
        return appointmentService.getById(appointmentId);
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponseDTO> getMyPatientAppointments() {
        log.info("[APPOINTMENT] Getting appointments for patient user: {}", currentUser().getId());
        return appointmentService.getPatientAppointments(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponseDTO> getMyDoctorAppointments() {
        log.info("[APPOINTMENT] Getting appointments for doctor user: {}", currentUser().getId());
        return appointmentService.getDoctorAppointments(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnAppointment(#appointmentId)")
    public AppointmentResponseDTO updateStatus(Long appointmentId, AppointmentStatusUpdateDTO dto) {
        log.info("[APPOINTMENT] Updating status of appointment: {} to {}", appointmentId, dto.status());
        return appointmentService.updateStatus(appointmentId, dto, currentUser().getId());
    }

    private UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}