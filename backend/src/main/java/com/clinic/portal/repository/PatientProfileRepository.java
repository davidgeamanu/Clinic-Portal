package com.clinic.portal.repository;

import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    // Primary lookup: given an authenticated User, get their patient data
    Optional<PatientProfile> findByUser(User user);

    // Used during registration to prevent creating duplicate profiles
    boolean existsByUser(User user);

    /**
     * Paginated search for the admin patients table. Filtering has to happen in
     * the query rather than on the returned page — a client-side filter would
     * only ever see the rows of the page it was given.
     *
     * An empty {@code search} matches everything; a null {@code active} means
     * "any account status".
     */
    @Query("""
            SELECT p FROM PatientProfile p JOIN p.user u
            WHERE (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<PatientProfile> search(@Param("search") String search,
                                @Param("active") Boolean active,
                                Pageable pageable);
}
