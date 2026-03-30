package com.queueless.queueless.repository;

import com.queueless.queueless.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {
    List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(UUID doctorId);

    long countByDoctorIdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);
}
