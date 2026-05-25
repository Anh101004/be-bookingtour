package com.bookingtour.payment.dto.response;

import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSummaryResponse {

    private String bookingId;
    private String bookingCode;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private BookingPaymentStatus paymentStatus;
    private LocalDate dueDate;
    private LocalDateTime lastPaymentDate;
    private List<PaymentResponse> payments;
}