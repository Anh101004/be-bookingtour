package com.bookingtour.schedule.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Thông tin HDV của 1 lịch khởi hành.
 * Trả về từ GET /api/v1/schedules/{scheduleId}/guide
 */
@Data
@Builder
public class ScheduleGuideResponse {

    private String guideId;
    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;
    private String languages;
    private String specialties;
    private String bio;
    private Integer experienceYears;
    private BigDecimal averageRating;
    private Integer totalTours;
    private String  status;       // AVAILABLE / ON_TOUR / ON_LEAVE / INACTIVE
    private String  statusLabel;
}