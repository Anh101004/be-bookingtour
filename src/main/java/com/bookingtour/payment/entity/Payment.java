package com.bookingtour.payment.entity;

import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name = "payment_id", length = 36)
    private String paymentId;

    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 50, nullable = false)
    @Builder.Default
    private PaymentType paymentType = PaymentType.DEPOSIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "payment_note", columnDefinition = "TEXT")
    private String paymentNote;

    @Column(name = "confirmed_by", length = 36)
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.paymentId == null || this.paymentId.isBlank()) {
            this.paymentId = java.util.UUID.randomUUID().toString();
        }
    }
}