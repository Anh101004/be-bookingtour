package com.bookingtour.payment.dto.response;

import com.bookingtour.payment.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundResponse {

    private String refundId;
    private String bookingId;
    private String paymentId;
    private BigDecimal refundAmount;
    private Integer refundPercent;
    private Integer daysBeforeTour;
    private String refundReason;
    private RefundStatus refundStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedBy;
    private String note;
}