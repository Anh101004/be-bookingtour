package com.bookingtour.guide.dto.response;

import com.bookingtour.guide.enums.GuideStatus;
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
public class GuideResponse {

    private String guideId;
    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;
    private String dateOfBirth;
    private String gender;
    private Integer experienceYears;
    private String languages;
    private String certifications;
    private String bio;
    private String specialties;
    private BigDecimal averageRating;
    private Integer totalTours;
    private GuideStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}