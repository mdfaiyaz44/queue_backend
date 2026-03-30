package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.BadRequestException;
import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.queue.DoctorQueueResponse;
import com.queueless.queueless.dto.queue.QueueTokenResponse;
import com.queueless.queueless.entity.Appointment;
import com.queueless.queueless.entity.AppointmentStatus;
import com.queueless.queueless.entity.NotificationType;
import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;
import com.queueless.queueless.entity.QueueToken;
import com.queueless.queueless.repository.QueueTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final QueueTokenRepository queueTokenRepository;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final QueueEventPublisher queueEventPublisher;
    private final QueueCacheService queueCacheService;
    private final NotificationService notificationService;

    public QueueService(
            QueueTokenRepository queueTokenRepository,
            AppointmentService appointmentService,
            DoctorService doctorService,
            QueueEventPublisher queueEventPublisher,
            QueueCacheService queueCacheService,
            NotificationService notificationService
    ) {
        this.queueTokenRepository = queueTokenRepository;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.queueEventPublisher = queueEventPublisher;
        this.queueCacheService = queueCacheService;
        this.notificationService = notificationService;
    }

    public DoctorQueueResponse getDoctorQueue(UUID doctorId, LocalDate date) {
        var cachedQueue = queueCacheService.getDoctorQueue(doctorId, date);
        if (cachedQueue.isPresent()) {
            return cachedQueue.get();
        }

        var doctor = doctorService.getDoctorEntity(doctorId);
        List<QueueToken> tokens = queueTokenRepository
                .findByAppointmentDoctorIdAndAppointmentAppointmentDateOrderByPriorityDescTokenNumberAsc(doctorId, date)
                .stream()
                .sorted(queueComparator())
                .toList();

        long waitingCount = tokens.stream().filter(token -> token.getQueueStatus() == QueueStatus.WAITING).count();
        long inProgressCount = tokens.stream().filter(token -> token.getQueueStatus() == QueueStatus.IN_PROGRESS).count();
        long completedCount = tokens.stream().filter(token -> token.getQueueStatus() == QueueStatus.DONE).count();

        List<QueueTokenResponse> responses = tokens.stream().map(this::toResponse).toList();
        DoctorQueueResponse response = new DoctorQueueResponse(
                doctorId,
                doctor.getUser().getName(),
                date,
                waitingCount,
                inProgressCount,
                completedCount,
                responses
        );
        queueCacheService.cacheDoctorQueue(response);
        return response;
    }

    public QueueTokenResponse getPatientQueue(UUID tokenId, Principal principal) {
        QueueToken token = queueTokenRepository.findDetailedById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        if (!token.getAppointment().getPatient().getEmail().equalsIgnoreCase(principal.getName())) {
            throw new BadRequestException("You can view only your own token");
        }
        return toResponse(token);
    }

    @Transactional
    public QueueTokenResponse startConsultation(UUID appointmentId, Principal principal) {
        QueueToken token = getDoctorOwnedToken(appointmentId, principal);
        if (token.getQueueStatus() != QueueStatus.WAITING) {
            throw new BadRequestException("Only waiting tokens can be started");
        }

        token.getAppointment().setStatus(AppointmentStatus.IN_CONSULTATION);
        token.setQueueStatus(QueueStatus.IN_PROGRESS);
        token.setStartedAt(LocalDateTime.now());
        queueEventPublisher.publish("TOKEN_STARTED", token);
        notificationService.createNotification(
                token.getAppointment().getPatient(),
                NotificationType.TURN_STARTED,
                "Your turn has started",
                "Please proceed for consultation with Dr. %s."
                        .formatted(token.getAppointment().getDoctor().getUser().getName())
        );
        queueCacheService.evictDoctorQueue(token.getAppointment().getDoctor().getId(), token.getAppointment().getAppointmentDate());
        log.info("Consultation started for appointment {}", appointmentId);
        return toResponse(token);
    }

    @Transactional
    public QueueTokenResponse completeConsultation(UUID appointmentId, Principal principal) {
        QueueToken token = getDoctorOwnedToken(appointmentId, principal);
        if (token.getQueueStatus() != QueueStatus.IN_PROGRESS) {
            throw new BadRequestException("Only in-progress tokens can be completed");
        }

        token.getAppointment().setStatus(AppointmentStatus.COMPLETED);
        token.setQueueStatus(QueueStatus.DONE);
        token.setCompletedAt(LocalDateTime.now());
        queueEventPublisher.publish("TOKEN_COMPLETED", token);
        notificationService.createNotification(
                token.getAppointment().getPatient(),
                NotificationType.TURN_COMPLETED,
                "Consultation completed",
                "Your consultation with Dr. %s has been marked complete."
                        .formatted(token.getAppointment().getDoctor().getUser().getName())
        );
        queueCacheService.evictDoctorQueue(token.getAppointment().getDoctor().getId(), token.getAppointment().getAppointmentDate());
        log.info("Consultation completed for appointment {}", appointmentId);
        return toResponse(token);
    }

    @Transactional
    public QueueTokenResponse skipConsultation(UUID appointmentId, Principal principal) {
        QueueToken token = getDoctorOwnedToken(appointmentId, principal);
        if (token.getQueueStatus() != QueueStatus.WAITING) {
            throw new BadRequestException("Only waiting tokens can be skipped");
        }

        token.getAppointment().setStatus(AppointmentStatus.NO_SHOW);
        token.setQueueStatus(QueueStatus.SKIPPED);
        token.setCompletedAt(LocalDateTime.now());
        queueEventPublisher.publish("TOKEN_SKIPPED", token);
        notificationService.createNotification(
                token.getAppointment().getPatient(),
                NotificationType.TURN_SKIPPED,
                "Queue token skipped",
                "Your queue token was marked skipped. Please contact the hospital if this is unexpected."
        );
        queueCacheService.evictDoctorQueue(token.getAppointment().getDoctor().getId(), token.getAppointment().getAppointmentDate());
        log.info("Consultation skipped for appointment {}", appointmentId);
        return toResponse(token);
    }

    private QueueToken getDoctorOwnedToken(UUID appointmentId, Principal principal) {
        Appointment appointment = appointmentService.getAppointmentEntity(appointmentId);
        if (!appointment.getDoctor().getUser().getEmail().equalsIgnoreCase(principal.getName())) {
            throw new BadRequestException("This appointment does not belong to the logged-in doctor");
        }
        return queueTokenRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue token not found"));
    }

    private QueueTokenResponse toResponse(QueueToken token) {
        List<QueueToken> doctorTokens = queueTokenRepository
                .findByAppointmentDoctorIdAndAppointmentAppointmentDateOrderByPriorityDescTokenNumberAsc(
                        token.getAppointment().getDoctor().getId(),
                        token.getAppointment().getAppointmentDate()
                )
                .stream()
                .sorted(queueComparator())
                .toList();

        int position = 0;
        for (QueueToken doctorToken : doctorTokens) {
            if (doctorToken.getQueueStatus() == QueueStatus.WAITING || doctorToken.getQueueStatus() == QueueStatus.IN_PROGRESS) {
                position++;
            }
            if (doctorToken.getId().equals(token.getId())) {
                break;
            }
        }
        if (token.getQueueStatus() == QueueStatus.DONE || token.getQueueStatus() == QueueStatus.CANCELLED || token.getQueueStatus() == QueueStatus.SKIPPED) {
            position = 0;
        }

        return new QueueTokenResponse(
                token.getId(),
                token.getAppointment().getId(),
                token.getTokenNumber(),
                token.getQueueStatus(),
                token.getPriority(),
                token.getPredictedWaitMinutes(),
                position,
                token.getAppointment().getPatient().getName(),
                token.getAppointment().getDoctor().getUser().getName(),
                token.getAppointment().getRecommendedArrivalTime(),
                token.getStartedAt(),
                token.getCompletedAt()
        );
    }

    private Comparator<QueueToken> queueComparator() {
        return Comparator.comparing((QueueToken token) -> priorityWeight(token.getPriority())).reversed()
                .thenComparing(QueueToken::getTokenNumber);
    }

    private int priorityWeight(PriorityLevel priority) {
        return switch (priority) {
            case EMERGENCY -> 4;
            case HIGH -> 3;
            case NORMAL -> 2;
            case LOW -> 1;
        };
    }
}
