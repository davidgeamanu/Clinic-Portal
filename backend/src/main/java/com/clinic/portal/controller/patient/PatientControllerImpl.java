package com.clinic.portal.controller.patient;

import com.clinic.portal.dto.patient.PatientProfileResponseDTO;
import com.clinic.portal.dto.patient.PatientProfileUpdateDTO;
import com.clinic.portal.security.UserDetailsImpl;
import com.clinic.portal.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientControllerImpl implements PatientController {

    private final PatientService patientService;

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public PatientProfileResponseDTO getMyProfile() {
        log.info("[PATIENT] Getting profile for user: {}", currentUser().getId());
        return patientService.getProfile(currentUser().getId());
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public PatientProfileResponseDTO updateMyProfile(PatientProfileUpdateDTO dto) {
        log.info("[PATIENT] Updating profile for user: {}", currentUser().getId());
        return patientService.updateProfile(currentUser().getId(), dto);
    }

    private UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}