package com.clinic.portal.repository;

import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActive(Role role, boolean active);

    long countByRole(Role role);

    long countByRoleAndActiveTrue(Role role);

    List<User> findByRoleAndCreatedAtBetween(Role role, LocalDateTime from, LocalDateTime to);
}
