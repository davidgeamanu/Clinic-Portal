package com.clinic.portal.security;

import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Long profileId = resolveProfileId(user);

        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(), profileId);
    }

    private Long resolveProfileId(User user) {
        if (user.getRole() == Role.PATIENT) {
            return patientProfileRepository.findByUser(user)
                    .map(p -> p.getId())
                    .orElse(null);
        }
        if (user.getRole() == Role.DOCTOR) {
            return doctorProfileRepository.findByUser(user)
                    .map(p -> p.getId())
                    .orElse(null);
        }
        return null; // ADMIN has no profile
    }
}