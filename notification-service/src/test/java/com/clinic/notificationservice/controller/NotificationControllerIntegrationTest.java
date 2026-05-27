package com.clinic.notificationservice.controller;

import com.clinic.notificationservice.model.Notification;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.repository.NotificationRepository;
import com.clinic.notificationservice.security.JwtService;
import com.clinic.notificationservice.security.Role;
import com.clinic.notificationservice.security.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationRepository notificationRepository;
    @Value("${app.jwt.secret}") private String jwtSecret;

    private Cookie jwtCookie;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        notificationRepository.save(Notification.builder()
                .userId(1L).message("Unread 1").type(NotificationType.APPOINTMENT_SCHEDULED).read(false).build());
        notificationRepository.save(Notification.builder()
                .userId(1L).message("Unread 2").type(NotificationType.APPOINTMENT_CONFIRMED).read(false).build());
        notificationRepository.save(Notification.builder()
                .userId(1L).message("Read").type(NotificationType.GENERAL).read(true).build());
        notificationRepository.save(Notification.builder()
                .userId(2L).message("Other user").type(NotificationType.GENERAL).read(false).build());

        jwtCookie = new Cookie("access_token", buildJwt(1L, "patient@test.com", "PATIENT", 1L));
    }

    @Test
    void getAll_returnsOnlyCurrentUsersNotifications() throws Exception {
        mockMvc.perform(get("/notifications/me").cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getAll_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/notifications/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAsRead_setsReadFlag() throws Exception {
        Notification unread = notificationRepository.findByUserIdAndReadFalse(1L).getFirst();

        mockMvc.perform(patch("/notifications/{id}/read", unread.getId()).cookie(jwtCookie))
                .andExpect(status().isNoContent());

        Notification updated = notificationRepository.findById(unread.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(updated.isRead());
    }

    @Test
    void markAsRead_otherUsersNotification_returns403() throws Exception {
        Notification otherUser = notificationRepository.findByUserIdOrderByCreatedAtDesc(2L).getFirst();

        mockMvc.perform(patch("/notifications/{id}/read", otherUser.getId()).cookie(jwtCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAllAsRead_marksAllUnread() throws Exception {
        mockMvc.perform(patch("/notifications/read-all").cookie(jwtCookie))
                .andExpect(status().isNoContent());

        long unread = notificationRepository.countByUserIdAndReadFalse(1L);
        org.junit.jupiter.api.Assertions.assertEquals(0, unread);
    }

    private String buildJwt(Long userId, String email, String role, Long profileId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("profileId", profileId)
                .claim("active", true)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(key)
                .compact();
    }
}
