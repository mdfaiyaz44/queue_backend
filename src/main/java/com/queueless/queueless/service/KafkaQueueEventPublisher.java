package com.queueless.queueless.service;

import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.config.KafkaTopicsProperties;
import com.queueless.queueless.dto.event.QueueEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaQueueEventPublisher.class);

    private final KafkaTemplate<String, QueueEventPayload> kafkaTemplate;
    private final AppProperties appProperties;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public KafkaQueueEventPublisher(
            KafkaTemplate<String, QueueEventPayload> kafkaTemplate,
            AppProperties appProperties,
            KafkaTopicsProperties kafkaTopicsProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.appProperties = appProperties;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    public void publish(QueueEventPayload payload) {
        if (!appProperties.features().kafkaEnabled()) {
            return;
        }

        try {
            kafkaTemplate.send(kafkaTopicsProperties.queueTopic(), payload.tokenId().toString(), payload);
            log.info("Kafka queue event published -> topic={}, eventType={}, tokenId={}",
                    kafkaTopicsProperties.queueTopic(), payload.eventType(), payload.tokenId());
        } catch (Exception exception) {
            log.warn("Kafka event publish failed, falling back to logs only: {}", exception.getMessage());
        }
    }
}
