package com.bookingtour.booking.dto.response;

import com.bookingtour.booking.enums.CancellationStatus;
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
public class CancellationResponse {

    private String requestId;
    private String bookingId;
    private String bookingCode;
    private String userId;
    private String reason;
    private Integer daysBeforeTour;
    private Integer refundPercent;
    private BigDecimal expectedRefund;
    private CancellationStatus status;
    private String adminNote;
    private String reviewedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}