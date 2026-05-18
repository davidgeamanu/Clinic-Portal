package com.clinic.portal.service;

import com.clinic.portal.dto.appointment.AppointmentRatingRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentRequestDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO bookAppointment(Long patientUserId, AppointmentRequestDTO dto);

    AppointmentResponseDTO getById(Long appointmentId);

    List<AppointmentResponseDTO> getPatientAppointments(Long patientUserId);

    List<AppointmentResponseDTO> getDoctorAppointments(Long doctorUserId);

    AppointmentResponseDTO updateStatus(Long appointmentId, AppointmentStatusUpdateDTO dto, Long requestingUserId);

    AppointmentResponseDTO rateAppointment(Long appointmentId, AppointmentRatingRequestDTO dto);
}
