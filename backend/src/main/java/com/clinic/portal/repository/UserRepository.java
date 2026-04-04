package com.clinic.portal.repository;

import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by login and registration to look up an account by email
    Optional<User> findByEmail(String email);

    // Used by registration to reject duplicate emails before saving
    boolean existsByEmail(String email);

    // Used by admin to list all users of a given role
    List<User> findByRole(Role role);

    // Used by admin to filter active/inactive users
    List<User> findByRoleAndActive(Role role, boolean active);
}
