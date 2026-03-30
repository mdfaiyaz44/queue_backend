package com.queueless.queueless.repository;

import com.queueless.queueless.entity.Appointment;
import com.queueless.queueless.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.department", "queueToken"})
    List<Appointment> findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(UUID patientId);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.department", "queueToken"})
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(UUID doctorId, LocalDate appointmentDate);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            List<AppointmentStatus> statuses
    );

    long countByDoctorIdAndAppointmentDateAndStatusIn(UUID doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.department", "queueToken"})
    Optional<Appointment> findDetailedById(UUID id);
}
