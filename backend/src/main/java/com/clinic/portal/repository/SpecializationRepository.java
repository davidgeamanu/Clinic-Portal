package com.clinic.portal.repository;

import com.clinic.portal.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    Optional<Specialization> findByName(String name);

    // Admin: prevent duplicate specialization names
    boolean existsByName(String name);
}
