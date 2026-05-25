package com.bookingtour.booking.entity;

import com.bookingtour.booking.enums.CancellationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationRequest {

    @Id
    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "days_before_tour", nullable = false)
    private Integer daysBeforeTour;

    @Column(name = "refund_percent", nullable = false)
    @Builder.Default
    private Integer refundPercent = 0;

    @Column(name = "expected_refund", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal expectedRefund = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private CancellationStatus status = CancellationStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        if (this.requestId == null || this.requestId.isBlank()) {
            this.requestId = java.util.UUID.randomUUID().toString();
        }
    }
}