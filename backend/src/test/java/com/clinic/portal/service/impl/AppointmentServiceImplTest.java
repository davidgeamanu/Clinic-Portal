package com.clinic.portal.service.impl;

import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.appointment.AppointmentStatusUpdateDTO;
import com.clinic.portal.exception.BusinessException;
import com.clinic.portal.mapper.AppointmentMapper;
import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.DoctorProfileRepository;
import com.clinic.portal.repository.PatientProfileRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.appointment.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientProfileRepository patientProfileRepository;
    @Mock private DoctorProfileRepository doctorProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppointmentMapper appointmentMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AppointmentStateRegistry stateRegistry;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        stateRegistry = new AppointmentStateRegistry(List.of(
                new ScheduledState(), new ConfirmedState(),
                new InProgressState(), new CompletedState(), new CancelledState()
        ));
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository, patientProfileRepository, doctorProfileRepository,
                userRepository, appointmentMapper, eventPublisher, stateRegistry
        );
    }

    @Test
    void updateStatus_scheduledToConfirmed_succeeds() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toDto(any())).thenReturn(mock(AppointmentResponseDTO.class));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.CONFIRMED);
        appointmentService.updateStatus(1L, dto, 10L);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void updateStatus_scheduledToInProgress_throwsBecauseNotAllowed() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().minusMinutes(5))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.IN_PROGRESS);
        assertThrows(BusinessException.class, () ->
                appointmentService.updateStatus(1L, dto, 10L));
    }

    @Test
    void updateStatus_confirmedToInProgress_setsStartedAt() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CONFIRMED)
                .scheduledAt(LocalDateTime.now().minusMinutes(5))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toDto(any())).thenReturn(mock(AppointmentResponseDTO.class));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.IN_PROGRESS);
        appointmentService.updateStatus(1L, dto, 10L);

        assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
        assertNotNull(appointment.getStartedAt());
    }

    @Test
    void updateStatus_inProgressToCompleted_setsCompletedAt() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.IN_PROGRESS)
                .scheduledAt(LocalDateTime.now().minusMinutes(30))
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toDto(any())).thenReturn(mock(AppointmentResponseDTO.class));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.COMPLETED);
        appointmentService.updateStatus(1L, dto, 10L);

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        assertNotNull(appointment.getCompletedAt());
    }

    @Test
    void updateStatus_completedToAnything_throws() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.COMPLETED)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.CANCELLED);
        assertThrows(BusinessException.class, () ->
                appointmentService.updateStatus(1L, dto, 10L));
    }

    @Test
    void updateStatus_cancelledToAnything_throws() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CANCELLED)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.CONFIRMED);
        assertThrows(BusinessException.class, () ->
                appointmentService.updateStatus(1L, dto, 10L));
    }

    @Test
    void updateStatus_confirmedToInProgress_blockedWhenFutureScheduledAt() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.CONFIRMED)
                .scheduledAt(LocalDateTime.now().plusHours(2))
                .build();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.IN_PROGRESS);
        assertThrows(Exception.class, () ->
                appointmentService.updateStatus(1L, dto, 10L));
    }

    @Test
    void updateStatus_publishesEventWithCorrectFields() {
        Appointment appointment = Appointment.builder()
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
        appointment.setId(5L);

        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toDto(any())).thenReturn(mock(AppointmentResponseDTO.class));

        var dto = new AppointmentStatusUpdateDTO(AppointmentStatus.CANCELLED);
        appointmentService.updateStatus(5L, dto, 99L);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());

        var event = (com.clinic.portal.event.AppointmentStatusChangedEvent) captor.getValue();
        assertEquals(5L, event.appointmentId());
        assertEquals(AppointmentStatus.SCHEDULED, event.previousStatus());
        assertEquals(AppointmentStatus.CANCELLED, event.newStatus());
        assertEquals(99L, event.actorUserId());
    }
}
