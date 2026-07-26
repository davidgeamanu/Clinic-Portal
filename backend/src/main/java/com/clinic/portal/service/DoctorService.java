package com.clinic.portal.service;

import com.clinic.portal.dto.doctor.AvailabilityWindowDTO;
import com.clinic.portal.dto.doctor.AvailableSlotDTO;
import com.clinic.portal.dto.doctor.BookedSlotDTO;
import com.clinic.portal.dto.doctor.DoctorPatientListItemDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.doctor.DoctorReviewDTO;
import com.clinic.portal.dto.doctor.PatientSummaryDTO;
import com.clinic.portal.dto.doctor.RecentPatientDTO;

import java.time.LocalDate;
import java.util.List;

public interface DoctorService {

    DoctorProfileResponseDTO getProfile(Long userId);

    DoctorProfileResponseDTO getProfileById(Long profileId);

    DoctorProfileResponseDTO updateProfile(Long userId, DoctorProfileUpdateDTO dto);

    List<DoctorProfileResponseDTO> getAllDoctors();

    List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId);

    List<RecentPatientDTO> getRecentPatients(Long doctorUserId);

    PatientSummaryDTO getPatientSummary(Long doctorUserId, Long patientProfileId);

    List<DoctorPatientListItemDTO> getDoctorPatients(Long doctorUserId);

    List<BookedSlotDTO> getBookedSlots(Long doctorProfileId, LocalDate date);

    List<AvailabilityWindowDTO> getMyAvailability(Long doctorUserId);

    List<AvailabilityWindowDTO> updateMyAvailability(Long doctorUserId, List<AvailabilityWindowDTO> windows);

    List<AvailabilityWindowDTO> getAvailability(Long doctorProfileId);

    List<AvailableSlotDTO> getAvailableSlots(Long doctorProfileId, LocalDate date, int durationMinutes);

    List<DoctorReviewDTO> getReviews(Long doctorProfileId);

    List<DoctorReviewDTO> getMyReviews(Long doctorUserId);
}
