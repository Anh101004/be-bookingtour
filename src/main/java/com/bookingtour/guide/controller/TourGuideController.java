package com.bookingtour.guide.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.guide.dto.request.GuideCreateRequest;
import com.bookingtour.guide.dto.request.GuideScheduleRequest;
import com.bookingtour.guide.dto.request.GuideUpdateRequest;
import com.bookingtour.guide.dto.response.GuideAvailabilityResponse;
import com.bookingtour.guide.dto.response.GuideResponse;
import com.bookingtour.guide.dto.response.GuideScheduleResponse;
import com.bookingtour.guide.service.ITourGuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class TourGuideController {

    private final ITourGuideService tourGuideService;

    // ==================== PUBLIC ====================

    /**
     * GET /api/guides/active
     * Lấy danh sách HDV đang hoạt động — Public
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GuideResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(tourGuideService.getAllActive()));
    }

    /**
     * GET /api/guides/{guideId}
     * Lấy thông tin một HDV — Public
     */
    @GetMapping("/{guideId}")
    public ResponseEntity<ApiResponse<GuideResponse>> getById(
            @PathVariable String guideId) {
        return ResponseEntity.ok(ApiResponse.success(tourGuideService.getById(guideId)));
    }

    /**
     * GET /api/guides/{guideId}/availability?startDate=&endDate=
     * Kiểm tra lịch rảnh của một HDV — Public
     */
    @GetMapping("/{guideId}/availability")
    public ResponseEntity<ApiResponse<GuideAvailabilityResponse>> checkAvailability(
            @PathVariable String guideId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                tourGuideService.checkAvailability(guideId, startDate, endDate)));
    }

    /**
     * GET /api/guides/available?startDate=&endDate=
     * Danh sách HDV rảnh trong khoảng ngày — Public
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<GuideAvailabilityResponse>>> getAvailableGuides(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                tourGuideService.getAvailableGuides(startDate, endDate)));
    }

    // ==================== ADMIN ====================

    /**
     * GET /api/guides
     * Lấy tất cả HDV (kể cả inactive) — ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GuideResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(tourGuideService.getAll()));
    }

    /**
     * POST /api/guides
     * Tạo HDV mới, hỗ trợ upload avatar cùng lúc — ADMIN
     * Content-Type: multipart/form-data
     * Fields: fullName, phone, email, ... (text) + avatarFile (file, optional)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuideResponse>> create(
            @Valid @ModelAttribute GuideCreateRequest request) {
        GuideResponse response = tourGuideService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo hướng dẫn viên thành công", response));
    }

    /**
     * PUT /api/guides/{guideId}
     * Cập nhật thông tin HDV, hỗ trợ đổi avatar cùng lúc — ADMIN
     * Content-Type: multipart/form-data
     * Fields: fullName, phone, email, ... (text) + avatarFile (file, optional)
     */
    @PutMapping(value = "/{guideId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuideResponse>> update(
            @PathVariable String guideId,
            @Valid @ModelAttribute GuideUpdateRequest request) {
        GuideResponse response = tourGuideService.update(guideId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật hướng dẫn viên thành công", response));
    }

    /**
     * POST /api/guides/{guideId}/avatar
     * Upload / đổi ảnh avatar riêng lẻ — ADMIN
     * Content-Type: multipart/form-data
     * Field: file (image)
     */
    @PostMapping(value = "/{guideId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable String guideId,
            @RequestPart("file") MultipartFile file) {
        String url = tourGuideService.uploadAvatar(guideId, file);
        return ResponseEntity.ok(ApiResponse.success("Upload avatar thành công", url));
    }

    /**
     * PATCH /api/guides/{guideId}/active?isActive=true|false
     * Kích hoạt / vô hiệu hóa HDV — ADMIN
     */
    @PatchMapping("/{guideId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuideResponse>> toggleActive(
            @PathVariable String guideId,
            @RequestParam boolean isActive) {
        GuideResponse response = tourGuideService.toggleActive(guideId, isActive);
        String msg = isActive ? "Đã kích hoạt hướng dẫn viên" : "Đã vô hiệu hóa hướng dẫn viên";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    // ==================== LỊCH LÀM VIỆC ====================

    /**
     * GET /api/guides/{guideId}/schedules
     * Xem lịch làm việc của HDV — ADMIN
     */
    @GetMapping("/{guideId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GuideScheduleResponse>>> getSchedules(
            @PathVariable String guideId) {
        return ResponseEntity.ok(
                ApiResponse.success(tourGuideService.getSchedules(guideId)));
    }

    /**
     * POST /api/guides/{guideId}/schedules
     * Thêm lịch làm việc cho HDV — ADMIN
     */
    @PostMapping("/{guideId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GuideScheduleResponse>> addSchedule(
            @PathVariable String guideId,
            @Valid @RequestBody GuideScheduleRequest request) {
        GuideScheduleResponse response = tourGuideService.addSchedule(guideId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm lịch làm việc thành công", response));
    }

    /**
     * DELETE /api/guides/{guideId}/schedules/{scheduleId}
     * Xóa lịch làm việc — ADMIN
     */
    @DeleteMapping("/{guideId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable String guideId,
            @PathVariable Integer scheduleId) {
        tourGuideService.deleteSchedule(guideId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch làm việc thành công"));
    }
}