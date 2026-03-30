package com.queueless.queueless.service;

import com.queueless.queueless.dto.event.QueueEventPayload;
import com.queueless.queueless.entity.QueueToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QueueEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QueueEventPublisher.class);

    private final KafkaQueueEventPublisher kafkaQueueEventPublisher;

    public QueueEventPublisher(KafkaQueueEventPublisher kafkaQueueEventPublisher) {
        this.kafkaQueueEventPublisher = kafkaQueueEventPublisher;
    }

    public void publish(String eventType, QueueToken queueToken) {
        QueueEventPayload payload = new QueueEventPayload(
                eventType,
                queueToken.getId(),
                queueToken.getAppointment().getId(),
                queueToken.getAppointment().getDoctor().getId(),
                queueToken.getAppointment().getPatient().getId(),
                queueToken.getTokenNumber(),
                queueToken.getQueueStatus(),
                queueToken.getPriority(),
                queueToken.getAppointment().getAppointmentDate(),
                LocalDateTime.now()
        );

        kafkaQueueEventPublisher.publish(payload);

        log.info("Queue event emitted -> eventType={}, tokenId={}, appointmentId={}",
                eventType, queueToken.getId(), queueToken.getAppointment().getId());
    }
}
