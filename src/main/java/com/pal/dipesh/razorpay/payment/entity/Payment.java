package com.pal.dipesh.razorpay.payment.entity;

import com.pal.dipesh.razorpay.common.enums.PaymentMethod;
import com.pal.dipesh.razorpay.common.enums.PaymentStatus;
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
 * Payment aggregate.
 *
 * <p>Intentionally does <strong>not</strong> extend {@code BaseAuditEntity}:
 * a payment is an event-driven aggregate, not a state-bag. Its lifecycle is
 * already captured precisely by dedicated write-once timestamps
 * ({@link #authorizedAt}, {@link #capturedAt}, {@link #settledAt},
 * {@link #failedAt}, {@link #refundedAt}) and by {@link PaymentTransitionLog}.
 * A generic {@code updatedAt}/{@code updatedBy} pair would be churned on every
 * gateway callback and would always be {@code "system"} — pure noise.
 *
 * <p>Only the immutable creation pair ({@code createdAt}, {@code createdBy})
 * is audited.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_merchant_id", columnList = "merchant_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_merchant_idempotency_key", columnList = "merchant_id, idempotency_key", unique = true)
        }
)
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class Payment {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, updatable = false)
	private OrderRecord orderRecord;

	@Column(name = "merchant_id", nullable = false, updatable = false)
	private UUID merchantId;

	@Column(name = "idempotency_key", nullable = false, length = 100)
	private String idempotencyKey;

	@Embedded
	// @AttributeOverride(name = "amountUnits", column = @Column(name = "unit_amount", nullable = false))
	private Money amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private PaymentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "method", nullable = false, length = 40)
	private PaymentMethod method;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "method_details", columnDefinition = "jsonb")
	private Map<String, Object> methodDetails;

	@Column(name = "bank_reference", length = 120)
	private String bankReference;

	@Column(name = "processor_reference", length = 120)
	private String processorReference;

	@Column(name = "error_code", length = 80)
	private String errorCode;

	@Column(name = "error_description", length = 500)
	private String errorDescription;

	@Column(name = "authorized_at")
	private LocalDateTime authorizedAt;

	@Column(name = "captured_at")
	private LocalDateTime capturedAt;

	@Column(name = "settled_at")
	private LocalDateTime settledAt;

	@Column(name = "failed_at")
	private LocalDateTime failedAt;

	@Column(name = "refunded_at")
	private LocalDateTime refundedAt;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(name = "created_by", updatable = false)
	private String createdBy;
}