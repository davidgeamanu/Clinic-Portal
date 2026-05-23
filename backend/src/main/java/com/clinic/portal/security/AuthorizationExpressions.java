package com.clinic.portal.security;

import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationExpressions {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_PATIENT = "ROLE_PATIENT";

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof UserDetailsImpl principal))
            return false;
        if (principal.getProfileId() == null)
            return false;
        return appointmentRepository.countByIdAndProfileId(appointmentId, principal.getProfileId()) > 0;
    }

    /**
     * Authorizes a status-update request against the appointment lifecycle rules.
     *
     * Admins may issue any transition on any appointment. Participants (the
     * patient or doctor on the appointment) may issue most transitions, with
     * one exception: only doctors and admins may confirm a SCHEDULED appointment
     * - patients cannot self-confirm.
     */
    public boolean canUpdateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        if (appointmentId == null || newStatus == null)
            return false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal))
            return false;

        if (hasRole(authentication, ROLE_ADMIN))
            return true;

        if (principal.getProfileId() == null)
            return false;
        boolean isParticipant = appointmentRepository.countByIdAndProfileId(appointmentId, principal.getProfileId()) > 0;
        if (!isParticipant)
            return false;

        // Patients may cancel their own appointment but cannot confirm it
        if (hasRole(authentication, ROLE_PATIENT) && newStatus == AppointmentStatus.CONFIRMED)
            return false;

        return true;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}