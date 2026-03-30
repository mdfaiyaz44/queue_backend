package com.queueless.queueless.service;

import com.queueless.queueless.dto.analytics.AnalyticsOverviewResponse;
import com.queueless.queueless.entity.AppointmentStatus;
import com.queueless.queueless.entity.QueueStatus;
import com.queueless.queueless.entity.Role;
import com.queueless.queueless.repository.AppointmentRepository;
import com.queueless.queueless.repository.DepartmentRepository;
import com.queueless.queueless.repository.DoctorRepository;
import com.queueless.queueless.repository.QueueTokenRepository;
import com.queueless.queueless.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class AnalyticsService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final DepartmentRepository departmentRepository;

    public AnalyticsService(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            QueueTokenRepository queueTokenRepository,
            DepartmentRepository departmentRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.departmentRepository = departmentRepository;
    }

    public AnalyticsOverviewResponse getOverview(LocalDate date) {
        long totalDoctors = doctorRepository.count();
        long totalPatients = userRepository.findByRole(Role.PATIENT).size();

        long totalAppointments = doctorRepository.findAll().stream()
                .mapToLong(doctor -> appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), date).size())
                .sum();

        long waitingTokens = doctorRepository.findAll().stream()
                .mapToLong(doctor -> queueTokenRepository.countByAppointmentDoctorIdAndAppointmentAppointmentDateAndQueueStatusIn(
                        doctor.getId(), date, List.of(QueueStatus.WAITING)))
                .sum();

        long inProgressTokens = doctorRepository.findAll().stream()
                .mapToLong(doctor -> queueTokenRepository.countByAppointmentDoctorIdAndAppointmentAppointmentDateAndQueueStatusIn(
                        doctor.getId(), date, List.of(QueueStatus.IN_PROGRESS)))
                .sum();

        long completedAppointments = doctorRepository.findAll().stream()
                .mapToLong(doctor -> appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), date)
                        .stream()
                        .filter(appointment -> appointment.getStatus() == AppointmentStatus.COMPLETED)
                        .count())
                .sum();

        var byDepartment = new LinkedHashMap<String, Long>();
        departmentRepository.findAll().forEach(department -> {
            long count = doctorRepository.findAll().stream()
                    .filter(doctor -> doctor.getDepartment().getId().equals(department.getId()))
                    .mapToLong(doctor -> appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), date).size())
                    .sum();
            byDepartment.put(department.getName(), count);
        });

        return new AnalyticsOverviewResponse(
                date,
                totalDoctors,
                totalPatients,
                totalAppointments,
                waitingTokens,
                inProgressTokens,
                completedAppointments,
                byDepartment
        );
    }
}
