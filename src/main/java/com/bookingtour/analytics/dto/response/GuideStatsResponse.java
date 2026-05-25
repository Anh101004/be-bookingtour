package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuideStatsResponse {
    private String     guideId;
    private String     fullName;
    private String     phone;
    private Integer    experienceYears;
    private String     languages;
    private long       totalTours;
    private long       completedTours;
    private BigDecimal averageRating;
    private long       totalReviews;
}