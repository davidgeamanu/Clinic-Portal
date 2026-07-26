package com.clinic.portal.event.listener;

import com.clinic.portal.event.AppointmentBookedEvent;
import com.clinic.portal.event.AppointmentStatusChangedEvent;
import com.clinic.portal.event.ConsultationNoteCreatedEvent;
import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.repository.AppointmentRepository;
import com.clinic.portal.repository.ConsultationNoteRepository;
import com.clinic.portal.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventRecorderTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ConsultationNoteRepository consultationNoteRepository;
    @Mock private OutboxEventRepository outboxEventRepository;

    private OutboxEventRecorder recorder;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        recorder = new OutboxEventRecorder(
                appointmentRepository, consultationNoteRepository, outboxEventRepository, new ObjectMapper());
        ReflectionTestUtils.setField(recorder, "appointmentBookedQueue", "test.booked");
        ReflectionTestUtils.setField(recorder, "appointmentStatusChangedQueue", "test.status");
        ReflectionTestUtils.setField(recorder, "consultationNoteCreatedQueue", "test.note");

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
    void onAppointmentBooked_writesOutboxRowWithFatPayload() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));

        recorder.onAppointmentBooked(new AppointmentBookedEvent(100L));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent row = captor.getValue();
        assertEquals("test.booked", row.getQueue());
        assertEquals("AppointmentBookedMessage", row.getEventType());
        assertNotNull(row.getEventId());
        assertNull(row.getPublishedAt());
        assertTrue(row.getPayload().contains("\"patientName\":\"John Doe\""));
        assertTrue(row.getPayload().contains("\"doctorEmail\":\"smith@test.com\""));
        assertTrue(row.getPayload().contains("\"appointmentId\":100"));
    }

    @Test
    void onAppointmentStatusChanged_writesOutboxRowWithAllFields() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));

        recorder.onAppointmentStatusChanged(new AppointmentStatusChangedEvent(
                100L, AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, 2L));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent row = captor.getValue();
        assertEquals("test.status", row.getQueue());
        assertTrue(row.getPayload().contains("\"previousStatus\":\"SCHEDULED\""));
        assertTrue(row.getPayload().contains("\"newStatus\":\"CONFIRMED\""));
        assertTrue(row.getPayload().contains("\"requestingUserId\":2"));
    }

    @Test
    void onConsultationNoteCreated_writesOutboxRowWithPatientData() {
        ConsultationNote note = new ConsultationNote();
        note.setId(50L);
        note.setAppointment(testAppointment);
        when(consultationNoteRepository.findById(50L)).thenReturn(Optional.of(note));

        recorder.onConsultationNoteCreated(new ConsultationNoteCreatedEvent(50L, 100L));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent row = captor.getValue();
        assertEquals("test.note", row.getQueue());
        assertTrue(row.getPayload().contains("\"noteId\":50"));
        assertTrue(row.getPayload().contains("\"patientEmail\":\"john@test.com\""));
    }
}
