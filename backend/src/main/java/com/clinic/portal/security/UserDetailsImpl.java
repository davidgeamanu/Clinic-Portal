package com.clinic.portal.security;

import com.clinic.portal.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Role role;
    private final Long profileId; // PatientProfile or DoctorProfile id; null for ADMIN
    private final boolean active;

    @NonNull
    @Override
    public String getUsername() {
        return email;
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}