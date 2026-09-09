package com.pal.dipesh.razorpay.payment.repository;

import com.pal.dipesh.razorpay.common.enums.OutboxStatus;
import com.pal.dipesh.razorpay.payment.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusOrderByCreatedAtDesc(OutboxStatus status);
}
