package com.pal.dipesh.razorpay.payment.outbox;

import com.pal.dipesh.razorpay.common.config.KafkaProperties;
import com.pal.dipesh.razorpay.common.enums.OutboxStatus;
import com.pal.dipesh.razorpay.payment.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxResultHandler outboxResultHandler;
    private final KafkaProperties kafkaProperties;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void poll(){
        var events = outboxEventRepository.findByStatusOrderByCreatedAtDesc(OutboxStatus.PENDING);

        for (var event : events) {
            try {
                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());

                Map<String, Object> envelope = Map.of(
                        "eventId", event.getId().toString(),
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType().name(),
                        "aggregateId", event.getAggregateId().toString(),
                        "data", event.getPayload()
                );

                kafkaTemplate.send(topic, key, envelope).get(5, TimeUnit.SECONDS);
                outboxResultHandler.handleEventPublished(event);
            } catch (Exception e) {
                log.error("Outbox event polling failed, eventId: {}, attempts: {}", event.getId(), event.getAttempts(), e);
                outboxResultHandler.handleEventFailed(event, e.getMessage());
            }
        }
    }

    private String extractMerchantId(Map<String, Object> payload){
        Object merchantId = payload.get("merchantId");

        return merchantId != null ? merchantId.toString() : "unknown";
    }
}
