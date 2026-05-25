package com.bookingtour.booking.entity;

import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.schedule.entity.TourSchedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @Column(name = "booking_code", length = 20, nullable = false, unique = true)
    private String bookingCode;

    // Thông tin khách hàng tại thời điểm đặt
    @Column(name = "customer_name", length = 255, nullable = false)
    private String customerName;

    @Column(name = "customer_email", length = 255, nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20, nullable = false)
    private String customerPhone;

    @Column(name = "num_adults", nullable = false)
    @Builder.Default
    private Integer numAdults = 1;

    @Column(name = "num_children", nullable = false)
    @Builder.Default
    private Integer numChildren = 0;

    // Tiền
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "deposit_percent", nullable = false)
    @Builder.Default
    private Integer depositPercent = 30;

    @Column(name = "paid_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    // Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, nullable = false)
    @Builder.Default
    private BookingPaymentStatus paymentStatus = BookingPaymentStatus.UNPAID;

    // Hạn thanh toán (3 ngày trước khởi hành)
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Trạng thái booking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "refund_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @PrePersist
    public void prePersist() {
        if (this.bookingId == null || this.bookingId.isBlank()) {
            this.bookingId = java.util.UUID.randomUUID().toString();
        }
        // Tính remaining
        this.remainingAmount = this.totalAmount.subtract(this.paidAmount);
        // Hạn thanh toán = 3 ngày trước khởi hành
        if (this.schedule != null && this.schedule.getDepartureDate() != null) {
            this.dueDate = this.schedule.getDepartureDate().minusDays(3);
        }
    }
}