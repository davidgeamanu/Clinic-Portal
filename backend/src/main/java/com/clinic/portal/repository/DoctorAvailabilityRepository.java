package com.clinic.portal.repository;

import com.clinic.portal.model.DoctorAvailability;
import com.clinic.portal.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctor_IdOrderByDayOfWeekAscStartTimeAsc(Long doctorProfileId);

    List<DoctorAvailability> findByDoctor_IdAndDayOfWeekOrderByStartTimeAsc(Long doctorProfileId, DayOfWeek dayOfWeek);

    void deleteByDoctor(DoctorProfile doctor);
}
