package com.pal.dipesh.razorpay.payment.entity;

import com.pal.dipesh.razorpay.common.enums.RefundStatus;
import com.pal.dipesh.razorpay.common.pojo.Money;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Refund aggregate.
 *
 * <p>Intentionally does <strong>not</strong> extend {@code BaseAuditEntity}:
 * a refund's lifecycle is {@code PENDING -> PROCESSED | FAILED}, captured by
 * {@link #processedAt} / {@link #failedAt}. A generic {@code updatedAt} would
 * tick once and never again, and {@code updatedBy} would always be
 * {@code "system"} (banks settle refunds asynchronously via webhook).
 *
 * <p>{@code createdAt} / {@code createdBy} are kept because the initiator
 * (usually a merchant dashboard user) is a critical financial audit signal.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "refund",
        indexes = {
                @Index(name = "idx_refund_payment_id", columnList = "payment_id"),
                @Index(name = "idx_refund_merchant_id", columnList = "merchant_id"),
                @Index(name = "idx_refund_status", columnList = "status")
        }
)
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class Refund {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 40)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_description", length = 500)
    private String errorDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes", columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
}