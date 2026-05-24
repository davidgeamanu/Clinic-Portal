package com.clinic.portal.controller;

import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void getPatientAppointments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/appointments/my/patient"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatientAppointments_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/appointments/my/patient")
                        .with(request -> {
                            var user = new UserDetailsImpl(9L, "patient@test.com", "", Role.PATIENT, 1L, true);
                            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getDoctorAppointments_asPatient_returns403() throws Exception {
        mockMvc.perform(get("/appointments/my/doctor")
                        .with(request -> {
                            var user = new UserDetailsImpl(9L, "patient@test.com", "", Role.PATIENT, 1L, true);
                            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            return request;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookAppointment_withInvalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(request -> {
                            var user = new UserDetailsImpl(9L, "patient@test.com", "", Role.PATIENT, 1L, true);
                            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }
}
