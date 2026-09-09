package com.pal.dipesh.razorpay.payment.entity;

import com.pal.dipesh.razorpay.common.enums.EventAggregateType;
import com.pal.dipesh.razorpay.common.enums.OutboxStatus;
import com.pal.dipesh.razorpay.common.pojo.BaseAuditEntity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "outbox_event",
        indexes = {
                @Index(name = "idx_outbox_event_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_outbox_event_aggregate", columnList = "aggregate_type, aggregate_id")
        }
)
@EqualsAndHashCode(callSuper = true)
public class OutboxEvent extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 40)
    private EventAggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
