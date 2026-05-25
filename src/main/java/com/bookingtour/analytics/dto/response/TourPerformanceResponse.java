package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourPerformanceResponse {
    private String     tourId;
    private String     tourTitle;
    private String     destination;
    private BigDecimal priceAdult;
    private long       totalBookings;
    private long       completedBookings;
    private long       cancelledBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private long       ratingCount;
    private long       viewCount;
    private double     cancellationRate;
}