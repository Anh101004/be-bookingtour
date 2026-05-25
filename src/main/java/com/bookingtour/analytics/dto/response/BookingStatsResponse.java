package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingStatsResponse {
    private long totalBookings;
    private long pendingBookings;
    private long depositedBookings;
    private long confirmedBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long unpaidBookings;
    private long depositedPayment;
    private long fullyPaidBookings;
    private Map<String, Long> bookingsByMonth;
}