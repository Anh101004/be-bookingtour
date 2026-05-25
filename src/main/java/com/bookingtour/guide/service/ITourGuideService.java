package com.bookingtour.guide.service;

import com.bookingtour.guide.dto.request.GuideCreateRequest;
import com.bookingtour.guide.dto.request.GuideScheduleRequest;
import com.bookingtour.guide.dto.request.GuideUpdateRequest;
import com.bookingtour.guide.dto.response.GuideAvailabilityResponse;
import com.bookingtour.guide.dto.response.GuideResponse;
import com.bookingtour.guide.dto.response.GuideScheduleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ITourGuideService {

    // ==================== Public ====================

    List<GuideResponse> getAllActive();

    GuideResponse getById(String guideId);

    GuideAvailabilityResponse checkAvailability(String guideId,
                                                LocalDate startDate,
                                                LocalDate endDate);

    List<GuideAvailabilityResponse> getAvailableGuides(LocalDate startDate,
                                                       LocalDate endDate);

    // ==================== Admin ====================

    List<GuideResponse> getAll();

    GuideResponse create(GuideCreateRequest request);

    GuideResponse update(String guideId, GuideUpdateRequest request);

    GuideResponse toggleActive(String guideId, boolean isActive);

    /** Upload / đổi avatar — endpoint riêng POST /api/guides/{id}/avatar */
    String uploadAvatar(String guideId, MultipartFile file);

    // ==================== Lịch làm việc ====================

    List<GuideScheduleResponse> getSchedules(String guideId);

    GuideScheduleResponse addSchedule(String guideId, GuideScheduleRequest request);

    void deleteSchedule(String guideId, Integer scheduleId);
}