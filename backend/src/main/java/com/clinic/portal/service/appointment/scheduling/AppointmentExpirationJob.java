package com.clinic.portal.service.appointment.scheduling;

import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.projection.StaleAppointmentView;
import com.clinic.portal.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled sweep that auto-cancels appointments left in {@link AppointmentStatus#SCHEDULED}
 * past their scheduled start time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpirationJob {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Scheduled(cron = "0 * * * * *")
    public void cancelStaleScheduledAppointments() {
        LocalDateTime now = LocalDateTime.now();
        List<StaleAppointmentView> stale = appointmentRepository.findStaleForCancellation(
                AppointmentStatus.SCHEDULED, now);

        if (stale.isEmpty()) return;

        log.info("[EXPIRATION] Found {} stale SCHEDULED appointment(s) past start time - cancelling", stale.size());

        AppointmentStatusUpdateDTO cancelDto = new AppointmentStatusUpdateDTO(AppointmentStatus.CANCELLED);
        for (StaleAppointmentView view : stale) {
            try {
                appointmentService.updateStatus(view.getId(), cancelDto, view.getDoctorUserId());
                log.info("[EXPIRATION] Auto-cancelled appointment {}", view.getId());
            } catch (Exception e) {
                log.warn("[EXPIRATION] Failed to auto-cancel appointment {}: {}",
                        view.getId(), e.getMessage());
            }
        }
    }
}