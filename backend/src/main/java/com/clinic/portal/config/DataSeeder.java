package com.clinic.portal.config;

import com.clinic.portal.model.Room;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;
import com.clinic.portal.repository.RoomRepository;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Order(1)
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    private record RoomSeed(String number, int floor, RoomType type, String specializationName) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSpecializations();
        seedRooms();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@clinic.com")) return;
        userRepository.save(User.builder()
                .email("admin@clinic.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("System")
                .lastName("Admin")
                .role(Role.ADMIN)
                .active(true)
                .build());
    }

    private void seedSpecializations() {
        List<String[]> specs = List.of(
                new String[]{"Cardiology",     "Diagnosis and treatment of heart conditions"},
                new String[]{"Neurology",      "Disorders of the nervous system"},
                new String[]{"Orthopedics",    "Musculoskeletal injuries and conditions"},
                new String[]{"Pediatrics",     "Medical care for infants, children and adolescents"},
                new String[]{"General Surgery","Surgical procedures of the abdomen and soft tissues"},
                new String[]{"Radiology",      "Medical imaging and diagnostics"},
                new String[]{"Emergency",      "Acute and emergency medical care"}
        );
        for (String[] s : specs) {
            if (!specializationRepository.existsByName(s[0])) {
                specializationRepository.save(Specialization.builder()
                        .name(s[0])
                        .description(s[1])
                        .build());
            }
        }
    }

    private void seedRooms() {
        if (roomRepository.count() > 0) return;

        List<RoomSeed> seeds = List.of(
                // Floor 4 — Neurology
                new RoomSeed("401", 4, RoomType.CONSULT,  "Neurology"),
                new RoomSeed("402", 4, RoomType.CONSULT,  "Neurology"),
                new RoomSeed("403", 4, RoomType.CONSULT,  "Neurology"),
                new RoomSeed("404", 4, RoomType.CONSULT,  "Neurology"),
                new RoomSeed("405", 4, RoomType.OR,       "Neurology"),
                new RoomSeed("406", 4, RoomType.OR,       "Neurology"),
                new RoomSeed("407", 4, RoomType.CONSULT,  null),
                new RoomSeed("408", 4, RoomType.CONSULT,  null),

                // Floor 3 — Cardiology
                new RoomSeed("301", 3, RoomType.CONSULT,  "Cardiology"),
                new RoomSeed("302", 3, RoomType.CONSULT,  "Cardiology"),
                new RoomSeed("303", 3, RoomType.CONSULT,  "Cardiology"),
                new RoomSeed("304", 3, RoomType.CONSULT,  "Cardiology"),
                new RoomSeed("305", 3, RoomType.OR,       "Cardiology"),
                new RoomSeed("306", 3, RoomType.OR,       "Cardiology"),
                new RoomSeed("307", 3, RoomType.CONSULT,  null),
                new RoomSeed("308", 3, RoomType.CONSULT,  null),

                // Floor 2 — Orthopedics + General Surgery
                new RoomSeed("201", 2, RoomType.CONSULT,  "Orthopedics"),
                new RoomSeed("202", 2, RoomType.CONSULT,  "Orthopedics"),
                new RoomSeed("203", 2, RoomType.OR,       "Orthopedics"),
                new RoomSeed("204", 2, RoomType.OR,       "Orthopedics"),
                new RoomSeed("205", 2, RoomType.CONSULT,  "General Surgery"),
                new RoomSeed("206", 2, RoomType.CONSULT,  "General Surgery"),
                new RoomSeed("207", 2, RoomType.OR,       "General Surgery"),
                new RoomSeed("208", 2, RoomType.OR,       "General Surgery"),

                // Floor 1 — Pediatrics
                new RoomSeed("101", 1, RoomType.CONSULT,  "Pediatrics"),
                new RoomSeed("102", 1, RoomType.CONSULT,  "Pediatrics"),
                new RoomSeed("103", 1, RoomType.CONSULT,  "Pediatrics"),
                new RoomSeed("104", 1, RoomType.CONSULT,  "Pediatrics"),
                new RoomSeed("105", 1, RoomType.OR,       "Pediatrics"),
                new RoomSeed("106", 1, RoomType.OR,       "Pediatrics"),
                new RoomSeed("107", 1, RoomType.CONSULT,  null),
                new RoomSeed("108", 1, RoomType.CONSULT,  null),

                // Floor 0 — Emergency + Radiology
                new RoomSeed("001", 0, RoomType.CONSULT,  "Emergency"),
                new RoomSeed("002", 0, RoomType.CONSULT,  "Emergency"),
                new RoomSeed("003", 0, RoomType.OR,       "Emergency"),
                new RoomSeed("004", 0, RoomType.OR,       "Emergency"),
                new RoomSeed("005", 0, RoomType.IMAGING,  "Radiology"),
                new RoomSeed("006", 0, RoomType.IMAGING,  "Radiology"),
                new RoomSeed("007", 0, RoomType.OR,       null),
                new RoomSeed("008", 0, RoomType.CONSULT,  null)
        );

        Map<String, Specialization> specByName = new java.util.HashMap<>();
        specializationRepository.findAll().forEach(s -> specByName.put(s.getName(), s));

        for (RoomSeed seed : seeds) {
            if (!roomRepository.existsByRoomNumber(seed.number())) {
                roomRepository.save(Room.builder()
                        .roomNumber(seed.number())
                        .floor(seed.floor())
                        .type(seed.type())
                        .status(RoomStatus.FREE)
                        .specialization(seed.specializationName() != null ? specByName.get(seed.specializationName()) : null)
                        .build());
            }
        }
    }
}