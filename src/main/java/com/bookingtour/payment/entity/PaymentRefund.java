package com.bookingtour.payment.entity;

import com.bookingtour.payment.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefund {

    @Id
    @Column(name = "refund_id", length = 36)
    private String refundId;

    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @Column(name = "payment_id", length = 36, nullable = false)
    private String paymentId;

    @Column(name = "refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "refund_percent", nullable = false)
    private Integer refundPercent;

    @Column(name = "days_before_tour", nullable = false)
    private Integer daysBeforeTour;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 50, nullable = false)
    @Builder.Default
    private RefundStatus refundStatus = RefundStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by", length = 36)
    private String processedBy;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @PrePersist
    public void prePersist() {
        if (this.refundId == null || this.refundId.isBlank()) {
            this.refundId = java.util.UUID.randomUUID().toString();
        }
    }
}