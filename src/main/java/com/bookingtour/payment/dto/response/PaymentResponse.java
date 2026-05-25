package com.bookingtour.payment.dto.response;

import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.PaymentType;
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
public class PaymentResponse {

    private String paymentId;
    private String bookingId;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private LocalDate dueDate;
    private String transactionId;
    private String paymentNote;
    private String confirmedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}