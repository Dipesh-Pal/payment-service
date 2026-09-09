package com.pal.dipesh.razorpay.payment.repository;

import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
import com.pal.dipesh.razorpay.payment.entity.Payment;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<List<Payment>> findByOrderRecord_Id(UUID orderId);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId AND p.merchantId = :merchantId")
    Optional<Payment> findByIdAndMerchantIdForUpdate(UUID paymentId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId")
    Optional<Payment> findByIdForUpdate(UUID paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :paymentStatus AND p.settledAt IS NULL")
    List<Payment> findByMerchantIdAndStatusForUpdate(UUID merchantId, PaymentStatus paymentStatus);
}