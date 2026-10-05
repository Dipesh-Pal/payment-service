package com.pal.dipesh.razorpay.payment.outbox;

import com.pal.dipesh.razorpay.common.enums.EventAggregateType;
import com.pal.dipesh.razorpay.payment.entity.OutboxEvent;
import com.pal.dipesh.razorpay.payment.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    /**
     * Writes the outbox row inside the caller's transaction so that the event and the
     * aggregate state change commit or roll back atomically.
     * <p>
     * {@link Propagation#MANDATORY} deliberately refuses to start a transaction: calling this
     * without an active one is a dual-write bug and fails fast. Isolation is intentionally left
     * at {@link org.springframework.transaction.annotation.Isolation#DEFAULT} because isolation
     * belongs to the physical transaction owned by the outermost boundary.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(EventAggregateType aggregateType, UUID aggregateId, String eventType, Map<String , Object> payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
