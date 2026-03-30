package com.queueless.queueless.service;

import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.dto.prediction.WaitTimePredictionResponse;
import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;
import com.queueless.queueless.repository.DoctorRepository;
import com.queueless.queueless.repository.QueueTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class PredictionService {

    private final QueueTokenRepository queueTokenRepository;
    private final DoctorRepository doctorRepository;
    private final AppProperties appProperties;

    public PredictionService(
            QueueTokenRepository queueTokenRepository,
            DoctorRepository doctorRepository,
            AppProperties appProperties
    ) {
        this.queueTokenRepository = queueTokenRepository;
        this.doctorRepository = doctorRepository;
        this.appProperties = appProperties;
    }

    public WaitTimePredictionResponse predict(UUID doctorId, LocalDate date, LocalTime appointmentTime, PriorityLevel priority) {
        var doctor = doctorRepository.findWithUserAndDepartmentById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        List<QueueStatus> waitingStatuses = List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS);
        int waitingPatients = (int) queueTokenRepository.countByAppointmentDoctorIdAndAppointmentAppointmentDateAndQueueStatusIn(
                doctorId, date, waitingStatuses
        );
        int emergencyPatientsAhead = (int) queueTokenRepository.countByAppointmentDoctorIdAndAppointmentAppointmentDateAndPriorityAndQueueStatusIn(
                doctorId, date, PriorityLevel.EMERGENCY, waitingStatuses
        );

        int baseMinutes = waitingPatients * doctor.getAverageConsultationMinutes();
        int emergencyPenalty = emergencyPatientsAhead * 8;
        int peakHourPenalty = isPeakHour(appointmentTime) ? 10 : 0;
        int priorityBoost = priority == PriorityLevel.EMERGENCY ? -15 : priority == PriorityLevel.HIGH ? -5 : 0;

        int predictedWait = Math.max(0, baseMinutes + emergencyPenalty + peakHourPenalty + doctor.getConsultationBufferMinutes() + priorityBoost);
        LocalDateTime estimatedStart = LocalDateTime.of(date, appointmentTime).plusMinutes(predictedWait);
        LocalDateTime recommendedArrival = estimatedStart.minusMinutes(priority == PriorityLevel.EMERGENCY ? 5 : 15);

        String mode = appProperties.features().aiEnabled() ? "AI_READY_HEURISTIC_PIPELINE" : "RULE_BASED_HEURISTIC";
        return new WaitTimePredictionResponse(
                doctorId,
                waitingPatients,
                emergencyPatientsAhead,
                predictedWait,
                estimatedStart,
                recommendedArrival,
                mode
        );
    }

    private boolean isPeakHour(LocalTime appointmentTime) {
        return !appointmentTime.isBefore(LocalTime.of(10, 0)) && !appointmentTime.isAfter(LocalTime.of(13, 0));
    }
}
