package com.pal.dipesh.razorpay.payment.entity;

import com.pal.dipesh.razorpay.common.enums.PaymentActor;
import com.pal.dipesh.razorpay.common.enums.PaymentEvent;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Append-only audit log: every {@link PaymentStatus} change on a
 * {@link Payment} produces one row here.
 *
 * <p>Intentionally does <strong>not</strong> extend {@code BaseAuditEntity}:
 * this <em>is</em> the audit log. {@link #occurredAt} is the creation
 * timestamp (a separate {@code createdAt} would be redundant), and
 * {@link #actor} is a domain-typed principal that's far more precise than a
 * generic {@code createdBy} string. Rows are never updated.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(
        name = "payment_transition_log",
        indexes = {
                @Index(name = "idx_ptl_payment_id", columnList = "payment_id"),
                @Index(name = "idx_ptl_occurred_at", columnList = "occurred_at")
        }
)
public class PaymentTransitionLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 40)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private PaymentEvent eventType;

    @Enumerated(EnumType.STRING)
    @Column (name = "actor", length = 100)
    private PaymentActor actor;

//    @Column(name = "reason")
//    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}