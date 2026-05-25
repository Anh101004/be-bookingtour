package com.bookingtour.booking.dto.response;

import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private String bookingId;
    private String bookingCode;
    private BookingStatus status;
    private BookingPaymentStatus paymentStatus;

    // Customer info
    private String userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Tour info
    private String scheduleId;
    private String tourId;
    private String tourTitle;
    private LocalDate departureDate;
    private LocalDate returnDate;

    // Số lượng
    private Integer numAdults;
    private Integer numChildren;

    // Tiền
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Integer depositPercent;
    private LocalDate dueDate;

    private String notes;
    private BigDecimal refundAmount;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
}