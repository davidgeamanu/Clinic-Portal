package com.clinic.portal.controller.doctor;

import com.clinic.portal.dto.doctor.BookedSlotDTO;
import com.clinic.portal.dto.doctor.DoctorPatientListItemDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.doctor.PatientSummaryDTO;
import com.clinic.portal.dto.doctor.RecentPatientDTO;
import com.clinic.portal.security.UserDetailsImpl;
import com.clinic.portal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorControllerImpl implements DoctorController {

    private final DoctorService doctorService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<DoctorProfileResponseDTO> getAllDoctors() {
        log.info("[DOCTOR] Getting all doctors");
        return doctorService.getAllDoctors();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public DoctorProfileResponseDTO getDoctorById(Long profileId) {
        log.info("[DOCTOR] Getting doctor by profileId: {}", profileId);
        return doctorService.getProfileById(profileId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<BookedSlotDTO> getBookedSlots(Long profileId, LocalDate date) {
        log.info("[DOCTOR] Getting booked slots for doctor {} on {}", profileId, date);
        return doctorService.getBookedSlots(profileId, date);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<DoctorProfileResponseDTO> getDoctorsBySpecialization(Long specializationId) {
        log.info("[DOCTOR] Getting doctors by specialization: {}", specializationId);
        return doctorService.getDoctorsBySpecialization(specializationId);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public List<RecentPatientDTO> getMyRecentPatients() {
        log.info("[DOCTOR] Getting recent patients for user: {}", currentUser().getId());
        return doctorService.getRecentPatients(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorPatientListItemDTO> getMyPatients() {
        log.info("[DOCTOR] Getting patient list for user: {}", currentUser().getId());
        return doctorService.getDoctorPatients(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public PatientSummaryDTO getPatientSummary(Long patientProfileId) {
        log.info("[DOCTOR] Getting patient summary for patientProfileId: {}", patientProfileId);
        return doctorService.getPatientSummary(currentUser().getId(), patientProfileId);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponseDTO getMyProfile() {
        log.info("[DOCTOR] Getting profile for user: {}", currentUser().getId());
        return doctorService.getProfile(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponseDTO updateMyProfile(DoctorProfileUpdateDTO dto) {
        log.info("[DOCTOR] Updating profile for user: {}", currentUser().getId());
        return doctorService.updateProfile(currentUser().getId(), dto);
    }

    private UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}