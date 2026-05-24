package com.clinic.portal.controller;

import com.clinic.portal.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public interface AuthenticatedController {

    default UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
