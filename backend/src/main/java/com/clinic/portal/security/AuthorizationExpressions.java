package com.clinic.portal.security;

import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationExpressions {

    private final AppointmentRepository appointmentRepository;

    // Returns true if the logged-in user's profile ID matches the given profileId
    public boolean isOwnProfile(Long profileId) {
        if (profileId == null)
            return false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getPrincipal() instanceof UserDetailsImpl principal &&
                principal.getProfileId() != null &&
                principal.getProfileId().equals(profileId);
    }

    // Returns true if the logged-in user (as patient or doctor) is a participant in the appointment
    public boolean isOwnAppointment(Long appointmentId) {
        if (appointmentId == null)
            return false;
        if (!appointmentRepository.existsById(appointmentId))
            throw new DataNotFoundException(ExceptionCode.APPOINTMENT_NOT_FOUND);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof UserDetailsImpl principal))
            return false;
        if (principal.getProfileId() == null)
            return false;
        return appointmentRepository.countByIdAndProfileId(appointmentId, principal.getProfileId()) > 0;
    }
}