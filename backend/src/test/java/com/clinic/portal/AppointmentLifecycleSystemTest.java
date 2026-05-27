package com.clinic.portal;

import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.security.JwtService;
import com.clinic.portal.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * System test: exercises the full appointment lifecycle through real
 * HTTP endpoints, real database, and real security filters.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentLifecycleSystemTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private Cookie patientCookie;
    private Cookie doctorCookie;
    private Long doctorProfileId;

    @BeforeEach
    void setUp() {
        // Create doctor user + profile (normally done by admin, but for test we do it directly)
        User doctorUser = User.builder()
                .email("system-test-doc@clinic.com")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Test").lastName("Doctor")
                .role(Role.DOCTOR).build();
        doctorUser = userRepository.save(doctorUser);

        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setUser(doctorUser);
        doctorProfile.setLicenseNumber("SYS-TEST-001");
        doctorProfile = doctorProfileRepository.save(doctorProfile);
        doctorProfileId = doctorProfile.getId();

        doctorCookie = jwtCookie(doctorUser, doctorProfile.getId());
    }

    @Test
    void fullLifecycle_registerBookConfirmStartComplete() throws Exception {
        // 1. Register a patient
        String registerJson = """
                {"email":"lifecycle@test.com","password":"Password123",
                 "firstName":"Test","lastName":"Patient","phoneNumber":"1234567890"}
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // Register doesn't set a cookie (login is handled by LoginAuthenticationFilter),
        // so look up the created user and mint a JWT directly.
        User patientUser = userRepository.findByEmail("lifecycle@test.com").orElseThrow();
        PatientProfile patientProfile = patientProfileRepository.findByUser(patientUser).orElseThrow();
        patientCookie = jwtCookie(patientUser, patientProfile.getId());

        // 2. Book an appointment (must be in the future to pass DTO validation)
        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(30);
        String bookJson = """
                {"doctorId":%d,"scheduledAt":"%s","durationMinutes":30,"mode":"IN_PERSON","reason":"System test"}
                """.formatted(doctorProfileId, scheduledAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        MvcResult bookResult = mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson)
                        .cookie(patientCookie))
                .andExpect(status().isCreated())
                .andReturn();

        AppointmentResponseDTO booked = objectMapper.readValue(
                bookResult.getResponse().getContentAsString(), AppointmentResponseDTO.class);
        assertNotNull(booked.id());
        assertEquals("SCHEDULED", booked.status().name());

        Long appointmentId = booked.id();

        // 3. Doctor confirms
        mockMvc.perform(patch("/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}""")
                        .cookie(doctorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Shift scheduledAt into the past so the IN_PROGRESS entry validator passes
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setScheduledAt(LocalDateTime.now().minusMinutes(5));
        appointmentRepository.saveAndFlush(appointment);

        // 4. Doctor starts
        mockMvc.perform(patch("/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PROGRESS"}""")
                        .cookie(doctorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startedAt").isNotEmpty());

        // 5. Doctor completes
        MvcResult completeResult = mockMvc.perform(patch("/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}""")
                        .cookie(doctorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andReturn();

        AppointmentResponseDTO completed = objectMapper.readValue(
                completeResult.getResponse().getContentAsString(), AppointmentResponseDTO.class);
        assertNotNull(completed.completedAt());
        assertNotNull(completed.startedAt());
    }

    @Test
    void patientCancel_fromScheduled() throws Exception {
        // Register + book
        String registerJson = """
                {"email":"cancel-test@test.com","password":"Password123",
                 "firstName":"Cancel","lastName":"Patient","phoneNumber":"0000000000"}
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        User cancelUser = userRepository.findByEmail("cancel-test@test.com").orElseThrow();
        PatientProfile cancelProfile = patientProfileRepository.findByUser(cancelUser).orElseThrow();
        patientCookie = jwtCookie(cancelUser, cancelProfile.getId());

        LocalDateTime scheduledAt = LocalDateTime.now().plusHours(2);
        String bookJson = """
                {"doctorId":%d,"scheduledAt":"%s","durationMinutes":30,"mode":"VIDEO"}
                """.formatted(doctorProfileId, scheduledAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        MvcResult bookResult = mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson)
                        .cookie(patientCookie))
                .andExpect(status().isCreated())
                .andReturn();

        AppointmentResponseDTO booked = objectMapper.readValue(
                bookResult.getResponse().getContentAsString(), AppointmentResponseDTO.class);

        // Patient cancels
        mockMvc.perform(patch("/appointments/{id}/status", booked.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CANCELLED"}""")
                        .cookie(patientCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private Cookie jwtCookie(User user, Long profileId) {
        var details = new UserDetailsImpl(user.getId(), user.getEmail(), "", user.getRole(), profileId, true);
        String token = jwtService.generateToken(details);
        return new Cookie("access_token", token);
    }
}
