package com.clinic.portal.controller;

import com.clinic.portal.model.enums.Role;
import com.clinic.portal.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    private RequestPostProcessor patient() {
        var user = new UserDetailsImpl(9L, "patient@test.com", "", Role.PATIENT, 1L, true);
        return authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void getPatientAppointments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/appointments/my/patient"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatientAppointments_authenticatedButUnknownUser_returns404() throws Exception {
        // Authentication passes the security layer; the empty test DB has no user 9,
        // so the service responds 404 rather than 401/403.
        mockMvc.perform(get("/appointments/my/patient").with(patient()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDoctorAppointments_asPatient_returns403() throws Exception {
        mockMvc.perform(get("/appointments/my/doctor").with(patient()))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookAppointment_withInvalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(patient()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookAppointment_withExcessiveDuration_returns400() throws Exception {
        // The UI always sends 30, but the endpoint is the contract: an unbounded
        // duration would reserve an arbitrarily long range in the doctor's calendar.
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"doctorId": 1, "scheduledAt": "2099-01-01T10:00:00", "durationMinutes": 100000}
                                """)
                        .with(patient()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookAppointment_withTooShortDuration_returns400() throws Exception {
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"doctorId": 1, "scheduledAt": "2099-01-01T10:00:00", "durationMinutes": 5}
                                """)
                        .with(patient()))
                .andExpect(status().isBadRequest());
    }
}
