package com.queueless.queueless.repository;

import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;
import com.queueless.queueless.entity.QueueToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueTokenRepository extends JpaRepository<QueueToken, UUID> {

    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    List<QueueToken> findByAppointmentDoctorIdAndAppointmentAppointmentDateOrderByPriorityDescTokenNumberAsc(UUID doctorId, LocalDate date);

    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    Optional<QueueToken> findByAppointmentId(UUID appointmentId);

    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    Optional<QueueToken> findDetailedById(UUID id);

    Integer countByAppointmentDoctorIdAndAppointmentAppointmentDateAndQueueStatusAndTokenNumberLessThanEqual(
            UUID doctorId,
            LocalDate date,
            QueueStatus queueStatus,
            Integer tokenNumber
    );

    long countByAppointmentDoctorIdAndAppointmentAppointmentDateAndQueueStatusIn(
            UUID doctorId,
            LocalDate date,
            List<QueueStatus> queueStatuses
    );

    long countByAppointmentDoctorIdAndAppointmentAppointmentDateAndPriorityAndQueueStatusIn(
            UUID doctorId,
            LocalDate date,
            PriorityLevel priority,
            List<QueueStatus> queueStatuses
    );
}
