package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.event.ConsultationNoteCreatedEvent;
import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ConsultationNoteRepository consultationNoteRepository;

    @InjectMocks
    private RabbitMqEventPublisher publisher;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "appointmentBookedQueue", "test.booked");
        ReflectionTestUtils.setField(publisher, "appointmentStatusChangedQueue", "test.status");
        ReflectionTestUtils.setField(publisher, "consultationNoteCreatedQueue", "test.note");

        User patientUser = User.builder()
                .firstName("John").lastName("Doe").email("john@test.com").build();
        patientUser.setId(1L);
        PatientProfile patient = new PatientProfile();
        patient.setUser(patientUser);

        User doctorUser = User.builder()
                .firstName("Dr").lastName("Smith").email("smith@test.com").build();
        doctorUser.setId(2L);
        DoctorProfile doctor = new DoctorProfile();
        doctor.setUser(doctorUser);

        testAppointment = Appointment.builder()
                .patient(patient).doctor(doctor)
                .scheduledAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .status(AppointmentStatus.SCHEDULED)
                .mode(AppointmentMode.IN_PERSON)
                .durationMinutes(30)
                .build();
        testAppointment.setId(100L);
    }

    @Test
    void onAppointmentBooked_publishesFatEventWithPatientAndDoctorData() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));

        publisher.onAppointmentBooked(new AppointmentBookedEvent(100L));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("test.booked"), captor.capture());

        var fatEvent = (com.clinic.portal.event.messaging.AppointmentBookedEvent) captor.getValue();
        assertEquals(100L, fatEvent.appointmentId());
        assertEquals("John Doe", fatEvent.patientName());
        assertEquals("john@test.com", fatEvent.patientEmail());
        assertEquals("Dr Smith", fatEvent.doctorName());
        assertEquals("smith@test.com", fatEvent.doctorEmail());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), fatEvent.scheduledAt());
        assertNotNull(fatEvent.eventId());
    }

    @Test
    void onAppointmentStatusChanged_publishesFatEventWithAllFields() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));

        var event = new AppointmentStatusChangedEvent(
                100L, AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, 2L);

        publisher.onAppointmentStatusChanged(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("test.status"), captor.capture());

        var fatEvent = (com.clinic.portal.event.messaging.AppointmentStatusChangedEvent) captor.getValue();
        assertEquals(AppointmentStatus.SCHEDULED, fatEvent.previousStatus());
        assertEquals(AppointmentStatus.CONFIRMED, fatEvent.newStatus());
        assertEquals(2L, fatEvent.requestingUserId());
        assertEquals(1L, fatEvent.patientUserId());
        assertEquals(2L, fatEvent.doctorUserId());
    }

    @Test
    void onConsultationNoteCreated_publishesFatEventWithScheduledAt() {
        ConsultationNote note = new ConsultationNote();
        note.setId(50L);
        note.setAppointment(testAppointment);

        when(consultationNoteRepository.findById(50L)).thenReturn(Optional.of(note));

        publisher.onConsultationNoteCreated(new ConsultationNoteCreatedEvent(50L, 100L));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("test.note"), captor.capture());

        var fatEvent = (com.clinic.portal.event.messaging.ConsultationNoteCreatedEvent) captor.getValue();
        assertEquals(50L, fatEvent.noteId());
        assertEquals(100L, fatEvent.appointmentId());
        assertEquals("John Doe", fatEvent.patientName());
        assertEquals("john@test.com", fatEvent.patientEmail());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), fatEvent.scheduledAt());
    }
}
