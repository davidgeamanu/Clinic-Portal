package com.clinic.portal.service.appointment.scheduling;

import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.projection.StaleAppointmentView;
import com.clinic.portal.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled sweep that cancels appointments left in {@link AppointmentStatus#SCHEDULED}
 * past their scheduled start time, so an unconfirmed booking stops blocking the
 * doctor's slot.
 *
 * These are cancellations, not no-shows: the appointment never reached a state
 * the patient could have attended. {@link AppointmentStatus#NO_SHOW} is reserved
 * for confirmed appointments and is only ever set by a doctor or admin.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpirationJob {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Scheduled(cron = "0 * * * * *")
    public void cancelStaleScheduledAppointments() {
        MDC.put("correlationId", "expiry-" + UUID.randomUUID().toString().substring(0, 8));
        try {
            cancelStaleAppointments();
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void cancelStaleAppointments() {
        LocalDateTime now = LocalDateTime.now();
        List<StaleAppointmentView> stale = appointmentRepository.findStaleForCancellation(
                AppointmentStatus.SCHEDULED, now);

        if (stale.isEmpty()) return;

        log.info("[EXPIRATION] Found {} unconfirmed appointment(s) past start time - cancelling", stale.size());

        AppointmentStatusUpdateDTO cancelDto = new AppointmentStatusUpdateDTO(AppointmentStatus.CANCELLED);
        for (StaleAppointmentView view : stale) {
            try {
                appointmentService.updateStatus(view.getId(), cancelDto, view.getDoctorUserId());
                log.info("[EXPIRATION] Auto-cancelled unconfirmed appointment {}", view.getId());
            } catch (Exception e) {
                log.warn("[EXPIRATION] Failed to cancel appointment {}: {}",
                        view.getId(), e.getMessage());
            }
        }
    }
}
