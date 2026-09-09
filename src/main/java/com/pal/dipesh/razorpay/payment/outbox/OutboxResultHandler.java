package com.pal.dipesh.razorpay.payment.outbox;

import com.pal.dipesh.razorpay.common.enums.OutboxStatus;
import com.pal.dipesh.razorpay.payment.entity.OutboxEvent;
import com.pal.dipesh.razorpay.payment.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxResultHandler {

    private final OutboxEventRepository outboxEventRepository;
    private static final Integer MAX_ATTEMPTS = 3;

    @Transactional
    public void handleEventPublished(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxStatus.PUBLISHED);
        outboxEvent.setPublishedAt(LocalDateTime.now());

        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void handleEventFailed(OutboxEvent event, String errorMessage) {
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(errorMessage.length() < 1000 ? errorMessage : errorMessage.substring(0, 1000));

        if(event.getAttempts() >= MAX_ATTEMPTS) {
            event.setStatus(OutboxStatus.FAILED);
        }

        outboxEventRepository.save(event);
    }
}
