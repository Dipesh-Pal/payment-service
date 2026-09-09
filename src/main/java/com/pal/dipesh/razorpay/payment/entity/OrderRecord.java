package com.pal.dipesh.razorpay.payment.entity;

import com.pal.dipesh.razorpay.common.enums.OrderStatus;
import com.pal.dipesh.razorpay.common.pojo.BaseAuditEntity;
import com.pal.dipesh.razorpay.common.pojo.Money;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Order to be paid — created by the merchant before a payment is attempted.
 *
 * <p>Extends {@link BaseAuditEntity} because an order is a true state-bag:
 * it transitions through {@link OrderStatus} (e.g. CREATED → ATTEMPTED →
 * PAID / EXPIRED), {@link #attempts} increments on each retry, and
 * {@link #notes} can be edited by the merchant. Generic last-modified
 * auditing is a meaningful "last touched" signal here.
 *
 * <p>{@code merchant_id} is denormalized (raw UUID, not a {@code @ManyToOne})
 * to keep order writes cheap and avoid lazy-loading the merchant on every
 * payment lookup.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "order_record",
        indexes = {
                @Index(name = "idx_order_merchant_id", columnList = "merchant_id"),
                @Index(name = "idx_order_status_expires_at", columnList = "order_status, expires_at"),
                @Index(name = "idx_order_merchant_receipt", columnList = "merchant_id, receipt", unique = true)
        }
)
@EqualsAndHashCode(callSuper = true)
public class OrderRecord extends BaseAuditEntity {

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Embedded
    private Money amount;

    @Column(name = "receipt", length = 100)
    private String receipt;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Column(name = "attempts",  nullable = false)
    @Builder.Default
    private int attempts = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes", columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}