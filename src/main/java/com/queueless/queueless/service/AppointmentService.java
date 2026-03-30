package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.BadRequestException;
import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.appointment.AppointmentResponse;
import com.queueless.queueless.dto.appointment.CreateAppointmentRequest;
import com.queueless.queueless.dto.prediction.WaitTimePredictionResponse;
import com.queueless.queueless.entity.Appointment;
import com.queueless.queueless.entity.AppointmentStatus;
import com.queueless.queueless.entity.Doctor;
import com.queueless.queueless.entity.DoctorAvailability;
import com.queueless.queueless.entity.NotificationType;
import com.queueless.queueless.entity.QueueStatus;
import com.queueless.queueless.entity.QueueToken;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.AppointmentRepository;
import com.queueless.queueless.repository.DoctorAvailabilityRepository;
import com.queueless.queueless.repository.QueueTokenRepository;
import com.queueless.queueless.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final List<AppointmentStatus> ACTIVE_APPOINTMENT_STATUSES = List.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.IN_CONSULTATION
    );

    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final UserRepository userRepository;
    private final DoctorService doctorService;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PredictionService predictionService;
    private final QueueEventPublisher queueEventPublisher;
    private final NotificationService notificationService;
    private final QueueCacheService queueCacheService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            QueueTokenRepository queueTokenRepository,
            UserRepository userRepository,
            DoctorService doctorService,
            DoctorAvailabilityRepository availabilityRepository,
            PredictionService predictionService,
            QueueEventPublisher queueEventPublisher,
            NotificationService notificationService,
            QueueCacheService queueCacheService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.userRepository = userRepository;
        this.doctorService = doctorService;
        this.availabilityRepository = availabilityRepository;
        this.predictionService = predictionService;
        this.queueEventPublisher = queueEventPublisher;
        this.notificationService = notificationService;
        this.queueCacheService = queueCacheService;
    }

    @Transactional
    public AppointmentResponse bookAppointment(CreateAppointmentRequest request, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorService.getDoctorEntity(request.doctorId());

        validateDoctorAvailability(doctor.getId(), request.appointmentDate(), request.appointmentTime());

        if (!doctor.isAcceptingAppointments()) {
            throw new BadRequestException("Doctor is not accepting appointments now");
        }

        boolean slotTaken = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
                doctor.getId(),
                request.appointmentDate(),
                request.appointmentTime(),
                ACTIVE_APPOINTMENT_STATUSES
        );
        if (slotTaken) {
            throw new BadRequestException("This appointment slot is already booked");
        }

        WaitTimePredictionResponse prediction = predictionService.predict(
                doctor.getId(), request.appointmentDate(), request.appointmentTime(), request.priority()
        );

        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.appointmentDate())
                .appointmentTime(request.appointmentTime())
                .status(AppointmentStatus.BOOKED)
                .priority(request.priority())
                .symptoms(request.symptoms())
                .notes(request.notes())
                .estimatedConsultationMinutes(doctor.getAverageConsultationMinutes())
                .estimatedStartTime(prediction.estimatedConsultationStart())
                .recommendedArrivalTime(prediction.recommendedArrivalTime())
                .build());

        int tokenNumber = (int) appointmentRepository.countByDoctorIdAndAppointmentDateAndStatusIn(
                doctor.getId(), request.appointmentDate(), ACTIVE_APPOINTMENT_STATUSES
        );
        QueueToken token = queueTokenRepository.save(QueueToken.builder()
                .appointment(appointment)
                .tokenNumber(tokenNumber)
                .queueStatus(QueueStatus.WAITING)
                .priority(request.priority())
                .predictedWaitMinutes(prediction.predictedWaitMinutes())
                .build());

        queueEventPublisher.publish("TOKEN_CREATED", token);
        notificationService.createNotification(
                patient,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Appointment booked",
                "Your appointment with Dr. %s is booked for %s at %s. Token number: %s."
                        .formatted(doctor.getUser().getName(), request.appointmentDate(), request.appointmentTime(), tokenNumber)
        );
        queueCacheService.evictDoctorQueue(doctor.getId(), request.appointmentDate());
        log.info("Appointment booked for patient {} with doctor {}", patient.getEmail(), doctor.getUser().getEmail());
        return getAppointmentById(appointment.getId());
    }

    public List<AppointmentResponse> getPatientAppointments(Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return appointmentRepository.findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(patient.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancelPatientAppointment(UUID appointmentId, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Appointment appointment = getAppointmentEntity(appointmentId);

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can cancel only your own appointments");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("This appointment cannot be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (appointment.getQueueToken() != null) {
            appointment.getQueueToken().setQueueStatus(QueueStatus.CANCELLED);
            queueEventPublisher.publish("TOKEN_CANCELLED", appointment.getQueueToken());
        }
        notificationService.createNotification(
                patient,
                NotificationType.APPOINTMENT_CANCELLED,
                "Appointment cancelled",
                "Your appointment on %s at %s has been cancelled."
                        .formatted(appointment.getAppointmentDate(), appointment.getAppointmentTime())
        );
        queueCacheService.evictDoctorQueue(appointment.getDoctor().getId(), appointment.getAppointmentDate());
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse markCheckedIn(UUID appointmentId, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Appointment appointment = getAppointmentEntity(appointmentId);
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can check in only for your own appointment");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BadRequestException("Only booked appointments can be checked in");
        }
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        notificationService.createNotification(
                patient,
                NotificationType.CHECK_IN_CONFIRMED,
                "Check-in confirmed",
                "You are checked in for your appointment with Dr. %s."
                        .formatted(appointment.getDoctor().getUser().getName())
        );
        queueCacheService.evictDoctorQueue(appointment.getDoctor().getId(), appointment.getAppointmentDate());
        return toResponse(appointment);
    }

    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        return toResponse(getAppointmentEntity(appointmentId));
    }

    public Appointment getAppointmentEntity(UUID appointmentId) {
        return appointmentRepository.findDetailedById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    private void validateDoctorAvailability(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        List<DoctorAvailability> schedules = availabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId);
        if (schedules.isEmpty()) {
            throw new BadRequestException("Doctor schedule is not configured yet");
        }

        boolean matchingSlot = schedules.stream().anyMatch(schedule ->
                schedule.getDayOfWeek() == appointmentDate.getDayOfWeek()
                        && !appointmentTime.isBefore(schedule.getStartTime())
                        && appointmentTime.isBefore(schedule.getEndTime())
        );

        if (!matchingSlot) {
            throw new BadRequestException("Doctor is not available for the selected date and time");
        }
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        QueueToken queueToken = appointment.getQueueToken();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getName(),
                appointment.getDoctor().getDepartment().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getPriority(),
                appointment.getSymptoms(),
                appointment.getNotes(),
                appointment.getEstimatedConsultationMinutes(),
                appointment.getEstimatedStartTime(),
                appointment.getRecommendedArrivalTime(),
                queueToken != null ? queueToken.getId() : null,
                queueToken != null ? queueToken.getTokenNumber() : null,
                queueToken != null ? queueToken.getQueueStatus() : null,
                queueToken != null ? queueToken.getPredictedWaitMinutes() : null
        );
    }
}
