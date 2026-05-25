package com.bookingtour.guide.dto.response;

import com.bookingtour.guide.enums.GuideStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuideAvailabilityResponse {

    private String guideId;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String languages;
    private Integer experienceYears;
    private BigDecimal averageRating;
    private GuideStatus status;
    private boolean available;
    private LocalDate checkFrom;
    private LocalDate checkTo;
}