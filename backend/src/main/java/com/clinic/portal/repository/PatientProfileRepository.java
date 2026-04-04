package com.clinic.portal.repository;

import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    // Primary lookup: given an authenticated User, get their patient data
    Optional<PatientProfile> findByUser(User user);

    // Used during registration to prevent creating duplicate profiles
    boolean existsByUser(User user);
}
