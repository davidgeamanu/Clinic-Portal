package com.clinic.portal.config;

import com.clinic.portal.model.*;
import com.clinic.portal.model.enums.*;
import com.clinic.portal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order(2)
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final RoomRepository roomRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail("sarahsmith@clinic.com")) return;

        Map<String, Specialization> specs = specializationRepository.findAll().stream()
                .collect(Collectors.toMap(Specialization::getName, s -> s));

        List<DoctorProfile> doctors = seedDoctors(specs);
        List<PatientProfile> patients = seedPatients();
        seedAppointmentsAndNotes(doctors, patients);
    }

    // ── Doctors ──────────────────────────────────────────────────────────

    private List<DoctorProfile> seedDoctors(Map<String, Specialization> specs) {
        record DoctorSeed(String email, String password, String firstName, String lastName,
                          String phone, String license, String biography, BigDecimal fee,
                          String specialization) {}

        List<DoctorSeed> seeds = List.of(
                new DoctorSeed(
                        "sarahsmith@clinic.com", "password123",
                        "Sarah", "Smith", "+40 721 334 556", "MD-2019-04821",
                        "Board-certified cardiologist with over 12 years of experience in interventional "
                                + "cardiology and heart failure management. Completed fellowship at Cleveland Clinic. "
                                + "Special interest in preventive cardiology and cardiac rehabilitation.",
                        new BigDecimal("350.00"), "Cardiology"),
                new DoctorSeed(
                        "michael.chen@clinic.com", "password123",
                        "Michael", "Chen", "+40 734 112 890", "MD-2015-07293",
                        "Pediatric specialist with 16 years of clinical experience. Former attending physician "
                                + "at Boston Children's Hospital. Passionate about childhood development, vaccination "
                                + "programs, and adolescent mental health.",
                        new BigDecimal("280.00"), "Pediatrics"),
                new DoctorSeed(
                        "elena.vasquez@clinic.com", "password123",
                        "Elena", "Vasquez", "+40 745 667 223", "MD-2017-03158",
                        "Neurologist specializing in headache disorders, epilepsy, and neurodegenerative diseases. "
                                + "Published researcher with 20+ peer-reviewed articles. Trained at Johns Hopkins "
                                + "Neurology Department. Fluent in English, Spanish, and Romanian.",
                        new BigDecimal("400.00"), "Neurology")
        );

        List<DoctorProfile> doctors = new ArrayList<>();
        for (DoctorSeed seed : seeds) {
            User user = userRepository.save(User.builder()
                    .email(seed.email())
                    .passwordHash(passwordEncoder.encode(seed.password()))
                    .firstName(seed.firstName())
                    .lastName(seed.lastName())
                    .phoneNumber(seed.phone())
                    .role(Role.DOCTOR)
                    .active(true)
                    .build());

            Specialization spec = specs.get(seed.specialization());
            List<Specialization> specList = new ArrayList<>(List.of(spec));

            DoctorProfile profile = DoctorProfile.builder()
                    .user(user)
                    .licenseNumber(seed.license())
                    .biography(seed.biography())
                    .consultationFee(seed.fee())
                    .specializations(specList)
                    .build();

            roomRepository.findFreeConsultRoomsForSpecialization(spec, RoomType.CONSULT, RoomStatus.FREE)
                    .stream().findFirst()
                    .ifPresent(profile::setRoom);

            doctors.add(doctorProfileRepository.save(profile));
        }
        return doctors;
    }

    // Patients

    private List<PatientProfile> seedPatients() {
        record PatientSeed(String email, String password, String firstName, String lastName,
                           String phone, LocalDate dob, Gender gender, BloodType bloodType,
                           String address, String emergencyName, String emergencyPhone,
                           Integer heightCm, BigDecimal weightKg,
                           String allergies, String conditions, String familyHistory,
                           String smoking, String alcohol, String exercise, String diet) {}

        List<PatientSeed> seeds = List.of(
                new PatientSeed(
                        "patient@clinic.com", "patient123",
                        "Maria", "Popescu", "+40 722 456 789",
                        LocalDate.of(1990, 3, 15), Gender.FEMALE, BloodType.A_POSITIVE,
                        "Strada Victoriei 45, Ap. 12, Bucharest",
                        "Ion Popescu", "+40 722 111 333",
                        165, new BigDecimal("62.50"),
                        "Penicillin, Shellfish",
                        "Mild asthma (childhood-onset, well controlled)",
                        "Father: hypertension, Type 2 diabetes. Mother: breast cancer (remission).",
                        "Never", "Occasionally", "3-4 times per week (running, yoga)", "Balanced"),
                new PatientSeed(
                        "andrei.ionescu@gmail.com", "patient123",
                        "Andrei", "Ionescu", "+40 733 890 112",
                        LocalDate.of(1985, 11, 2), Gender.MALE, BloodType.O_NEGATIVE,
                        "Bulevardul Unirii 78, Ap. 3, Cluj-Napoca",
                        "Diana Ionescu", "+40 733 445 667",
                        182, new BigDecimal("88.30"),
                        "None known",
                        "Hypertension (Stage 1, medicated), Seasonal allergies",
                        "Father: coronary artery disease (MI at age 58). Paternal grandfather: stroke.",
                        "Former (quit 2020)", "1-2 drinks per week", "Twice per week (gym)", "Low sodium"),
                new PatientSeed(
                        "sophie.martin@gmail.com", "patient123",
                        "Sophie", "Martin", "+40 756 223 441",
                        LocalDate.of(1998, 7, 22), Gender.FEMALE, BloodType.B_POSITIVE,
                        "Strada Eroilor 12, Sibiu",
                        "Claire Martin", "+40 756 001 002",
                        170, new BigDecimal("58.00"),
                        "Latex, Ibuprofen",
                        "Migraine with aura (diagnosed 2019)",
                        "Mother: migraines. No significant family history otherwise.",
                        "Never", "Rarely", "Daily (cycling, swimming)", "Vegetarian")
        );

        List<PatientProfile> patients = new ArrayList<>();
        for (PatientSeed seed : seeds) {
            User user = userRepository.save(User.builder()
                    .email(seed.email())
                    .passwordHash(passwordEncoder.encode(seed.password()))
                    .firstName(seed.firstName())
                    .lastName(seed.lastName())
                    .phoneNumber(seed.phone())
                    .role(Role.PATIENT)
                    .active(true)
                    .build());

            patients.add(patientProfileRepository.save(PatientProfile.builder()
                    .user(user)
                    .dateOfBirth(seed.dob())
                    .gender(seed.gender())
                    .bloodType(seed.bloodType())
                    .address(seed.address())
                    .emergencyContactName(seed.emergencyName())
                    .emergencyContactPhone(seed.emergencyPhone())
                    .heightCm(seed.heightCm())
                    .weightKg(seed.weightKg())
                    .allergies(seed.allergies())
                    .chronicConditions(seed.conditions())
                    .familyHistory(seed.familyHistory())
                    .lifestyleSmoking(seed.smoking())
                    .lifestyleAlcohol(seed.alcohol())
                    .lifestyleExercise(seed.exercise())
                    .lifestyleDiet(seed.diet())
                    .build()));
        }
        return patients;
    }

    // Appointments & Consultation Notes

    private void seedAppointmentsAndNotes(List<DoctorProfile> doctors, List<PatientProfile> patients) {
        DoctorProfile drSmith = doctors.get(0);    // Cardiology
        DoctorProfile drChen = doctors.get(1);     // Pediatrics
        DoctorProfile drVasquez = doctors.get(2);  // Neurology

        PatientProfile maria = patients.get(0);
        PatientProfile andrei = patients.get(1);
        PatientProfile sophie = patients.get(2);

        LocalDateTime now = LocalDateTime.now();

        // COMPLETED appointments (past) with consultation notes & ratings ──

        Appointment a1 = saveAppointment(maria, drSmith,
                now.minusDays(45).withHour(9).withMinute(0).withSecond(0).withNano(0),
                30, AppointmentMode.IN_PERSON, AppointmentStatus.COMPLETED,
                "Annual cardiac check-up and ECG review",
                now.minusDays(45).withHour(9).withMinute(2),
                now.minusDays(45).withHour(9).withMinute(28),
                5, "Dr. Smith was incredibly thorough. She took the time to explain my ECG results in detail and answered all my questions. Highly recommend.");

        saveNote(a1,
                "Normal sinus rhythm on ECG. Blood pressure 118/76 mmHg. Total cholesterol 195 mg/dL. No signs of cardiac abnormality.",
                "Continue current lifestyle. Repeat lipid panel in 12 months. Low-dose aspirin not indicated at this time.",
                "None required",
                "Patient is in excellent cardiovascular health for her age. Discussed importance of maintaining exercise routine. Family history of hypertension warrants annual monitoring.");

        Appointment a2 = saveAppointment(andrei, drSmith,
                now.minusDays(30).withHour(10).withMinute(30).withSecond(0).withNano(0),
                45, AppointmentMode.IN_PERSON, AppointmentStatus.COMPLETED,
                "Follow-up for hypertension management, medication review",
                now.minusDays(30).withHour(10).withMinute(33),
                now.minusDays(30).withHour(11).withMinute(12),
                4, "Very knowledgeable doctor. Adjusted my medication which has made a real difference. The wait was a bit long but the consultation itself was excellent.");

        saveNote(a2,
                "Stage 1 hypertension, improving. BP today 134/86 mmHg (previously 148/94). Heart rate 72 bpm. No end-organ damage on fundoscopy.",
                "Increase Lisinopril from 10mg to 20mg daily. Continue low-sodium diet. Home BP monitoring twice daily for 2 weeks, then weekly.",
                "Lisinopril 20mg — 1 tablet daily in the morning\nAmlodipine 5mg — 1 tablet daily (if BP remains above 130/80 after 4 weeks)",
                "Patient is responding to antihypertensive therapy but not yet at target. Given family history of coronary disease, aggressive BP control is warranted. Recheck in 6 weeks.");

        Appointment a3 = saveAppointment(sophie, drVasquez,
                now.minusDays(21).withHour(14).withMinute(0).withSecond(0).withNano(0),
                40, AppointmentMode.IN_PERSON, AppointmentStatus.COMPLETED,
                "Recurring migraines with visual aura, increasing frequency",
                now.minusDays(21).withHour(14).withMinute(1),
                now.minusDays(21).withHour(14).withMinute(38),
                5, "Dr. Vasquez really listened to my concerns. She created a comprehensive migraine diary plan and explained the triggers clearly. I finally feel like someone understands my condition.");

        saveNote(a3,
                "Migraine with aura (ICD-10: G43.1). Frequency increased from 2/month to 5/month over last 3 months. Aura consists of visual scotoma lasting 15-20 minutes. No neurological deficits on examination.",
                "Initiate preventive therapy. Avoid identified triggers (irregular sleep, bright screens, dehydration). Maintain migraine diary. MRI brain to rule out structural causes.",
                "Topiramate 25mg — 1 tablet at bedtime, increase to 50mg after 2 weeks\nSumatriptan 50mg — as needed for acute attacks (max 2 per week)\nMetoclopramide 10mg — for nausea during attacks",
                "Young patient with escalating migraine frequency. Latex and ibuprofen allergies noted — NSAIDs contraindicated. Starting low-dose topiramate for prevention. MRI ordered as baseline given family history. Follow up in 4 weeks to assess response.");

        Appointment a4 = saveAppointment(maria, drChen,
                now.minusDays(14).withHour(11).withMinute(0).withSecond(0).withNano(0),
                30, AppointmentMode.VIDEO, AppointmentStatus.COMPLETED,
                "Persistent cough in 5-year-old son, seeking pediatric advice",
                now.minusDays(14).withHour(11).withMinute(0),
                now.minusDays(14).withHour(11).withMinute(26),
                5, "Dr. Chen was wonderful with the video consultation. Very patient and gave clear instructions. My son's cough cleared up within a week following his advice.");

        saveNote(a4,
                "Parent reports 2-week history of productive cough in 5-year-old child (not the patient themselves — discussed by proxy via video). No fever, no wheezing. Likely post-viral cough.",
                "Symptomatic management. Honey (1 tsp) before bedtime for cough suppression. Humidifier in bedroom. Return if fever develops or cough persists beyond 3 weeks.",
                "Honey-based cough syrup — as directed on label\nSaline nasal spray — 2 sprays each nostril, 3 times daily",
                "Reassured parent. Post-viral cough is self-limiting. No antibiotics indicated. Phone follow-up if symptoms worsen.");

        Appointment a5 = saveAppointment(andrei, drVasquez,
                now.minusDays(10).withHour(15).withMinute(30).withSecond(0).withNano(0),
                30, AppointmentMode.IN_PERSON, AppointmentStatus.COMPLETED,
                "Intermittent tingling in left hand and forearm",
                now.minusDays(10).withHour(15).withMinute(32),
                now.minusDays(10).withHour(16).withMinute(0),
                4, "Thorough neurological exam. Dr. Vasquez was very reassuring and ordered the right tests. Waiting for nerve conduction results now.");

        saveNote(a5,
                "Intermittent paresthesia in left C6-C7 dermatome. No motor weakness. Reflexes symmetric. Tinel's sign negative at carpal tunnel. Likely cervical radiculopathy from prolonged desk work.",
                "Ergonomic workplace assessment. Cervical stretching exercises (handout provided). Nerve conduction study if symptoms persist beyond 4 weeks.",
                "Gabapentin 100mg — 1 tablet at bedtime for 2 weeks (for neuropathic discomfort)\nIbuprofen 400mg — as needed (patient has no NSAID allergy)",
                "Symptoms consistent with mild cervical radiculopathy. No red flags. Conservative management first. NCS only if no improvement in 4 weeks.");

        // CONFIRMED appointments (upcoming)

        saveAppointment(maria, drSmith,
                now.plusDays(3).withHour(9).withMinute(30).withSecond(0).withNano(0),
                30, AppointmentMode.IN_PERSON, AppointmentStatus.CONFIRMED,
                "Follow-up cardiac check, review updated lipid panel results",
                null, null, null, null);

        saveAppointment(sophie, drVasquez,
                now.plusDays(5).withHour(14).withMinute(0).withSecond(0).withNano(0),
                40, AppointmentMode.IN_PERSON, AppointmentStatus.CONFIRMED,
                "Migraine follow-up — review MRI results and topiramate response",
                null, null, null, null);

        // SCHEDULED appointments (upcoming, not yet confirmed)

        saveAppointment(andrei, drSmith,
                now.plusDays(8).withHour(11).withMinute(0).withSecond(0).withNano(0),
                45, AppointmentMode.IN_PERSON, AppointmentStatus.SCHEDULED,
                "6-week hypertension follow-up, BP medication dosage review",
                null, null, null, null);

        saveAppointment(sophie, drChen,
                now.plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0),
                30, AppointmentMode.VIDEO, AppointmentStatus.SCHEDULED,
                "General wellness check and vaccination review",
                null, null, null, null);

        // CANCELLED appointment

        saveAppointment(andrei, drChen,
                now.minusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0),
                30, AppointmentMode.VIDEO, AppointmentStatus.CANCELLED,
                "Mild fever and sore throat consultation",
                null, null, null, null);

        // Recompute doctor ratings
        recomputeRating(drSmith);
        recomputeRating(drChen);
        recomputeRating(drVasquez);
    }

    private Appointment saveAppointment(PatientProfile patient, DoctorProfile doctor,
                                        LocalDateTime scheduledAt, int durationMinutes,
                                        AppointmentMode mode, AppointmentStatus status,
                                        String reason,
                                        LocalDateTime startedAt, LocalDateTime completedAt,
                                        Integer rating, String review) {
        Appointment appt = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(scheduledAt)
                .durationMinutes(durationMinutes)
                .mode(mode)
                .status(status)
                .reason(reason)
                .build();
        appt.setStartedAt(startedAt);
        appt.setCompletedAt(completedAt);
        appt.setRating(rating);
        appt.setReview(review);
        return appointmentRepository.save(appt);
    }

    private void saveNote(Appointment appointment, String diagnosis, String treatment,
                          String prescription, String notes) {
        consultationNoteRepository.save(ConsultationNote.builder()
                .appointment(appointment)
                .diagnosis(diagnosis)
                .treatment(treatment)
                .prescription(prescription)
                .notes(notes)
                .build());
    }

    private void recomputeRating(DoctorProfile doctor) {
        var avg = appointmentRepository.findByDoctorOrderByScheduledAtDesc(doctor).stream()
                .map(Appointment::getRating)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        doctor.setRating(avg.isPresent()
                ? BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                : null);
        doctorProfileRepository.save(doctor);
    }
}
