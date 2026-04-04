package com.clinic.portal.repository;

import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    // Primary lookup: given an authenticated User, get their doctor data
    Optional<DoctorProfile> findByUser(User user);

    // Patient-facing: search doctors by specialization (the core discovery feature)
    List<DoctorProfile> findBySpecializationsContaining(Specialization specialization);

    // Admin: check license uniqueness before creating a doctor profile
    boolean existsByLicenseNumber(String licenseNumber);
}
