package com.bookingtour.invoice.entity;

import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Column(name = "invoice_id", length = 36)
    private String invoiceId;

    // ── Liên kết ──────────────────────────────────────────────
    @Column(name = "booking_id", length = 36, nullable = false)
    private String bookingId;

    @Column(name = "payment_id", length = 36)
    private String paymentId;

    // ── Số hóa đơn & loại ─────────────────────────────────────
    @Column(name = "invoice_number", length = 30, nullable = false, unique = true)
    private String invoiceNumber;           // VD: INV-2026-00001

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", length = 20, nullable = false)
    private InvoiceType invoiceType;        // DEPOSIT/REMAINING/FULL/REFUND

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "issued_by", length = 36)
    private String issuedBy;               // null = tự động

    // ── Snapshot thông tin khách hàng ─────────────────────────
    @Column(name = "customer_name", length = 255, nullable = false)
    private String customerName;

    @Column(name = "customer_email", length = 255, nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20, nullable = false)
    private String customerPhone;

    // ── Snapshot thông tin tour ────────────────────────────────
    @Column(name = "tour_title", length = 255, nullable = false)
    private String tourTitle;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "departure_location", length = 255)
    private String departureLocation;

    @Column(name = "destination", length = 255)
    private String destination;

    @Column(name = "guide_name", length = 255)
    private String guideName;

    // ── Chi tiết số lượng & đơn giá ───────────────────────────
    @Column(name = "num_adults", nullable = false)
    private Integer numAdults;

    @Column(name = "num_children", nullable = false)
    @Builder.Default
    private Integer numChildren = 0;

    @Column(name = "price_adult", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAdult;

    @Column(name = "price_child", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal priceChild = BigDecimal.ZERO;

    // ── Tiền ──────────────────────────────────────────────────
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;         // Tổng tiền toàn đơn

    @Column(name = "invoice_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal invoiceAmount;       // Số tiền lần thanh toán này

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO; // Đã trả trước đó

    @Column(name = "remaining_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal remainingAmount = BigDecimal.ZERO; // Còn lại

    @Column(name = "due_date")
    private LocalDate dueDate;             // Hạn thanh toán phần còn lại

    // ── File PDF ──────────────────────────────────────────────
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (this.invoiceId == null || this.invoiceId.isBlank()) {
            this.invoiceId = java.util.UUID.randomUUID().toString();
        }
    }
}