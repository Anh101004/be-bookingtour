package com.bookingtour.analytics.controller;

import com.bookingtour.analytics.dto.response.*;
import com.bookingtour.analytics.service.IAnalyticsService;
import com.bookingtour.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    /** GET /api/analytics/dashboard */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboard()));
    }

    /** GET /api/analytics/bookings?from=2026-01-01&to=2026-12-31 */
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingStatsResponse>> getBookingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getBookingStats(from, to)));
    }

    /** GET /api/analytics/revenue?from=&to= */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRevenueStats(from, to)));
    }

    /** GET /api/analytics/tours */
    @GetMapping("/tours")
    public ResponseEntity<ApiResponse<List<TourPerformanceResponse>>> getTourPerformance() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTourPerformance()));
    }

    /** GET /api/analytics/tours/{tourId} */
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<ApiResponse<TourPerformanceResponse>> getTourById(
            @PathVariable String tourId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTourPerformanceById(tourId)));
    }

    /** GET /api/analytics/tours/top?limit=5 */
    @GetMapping("/tours/top")
    public ResponseEntity<ApiResponse<List<TourPerformanceResponse>>> getTopTours(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTopTours(limit)));
    }

    /** GET /api/analytics/guides */
    @GetMapping("/guides")
    public ResponseEntity<ApiResponse<List<GuideStatsResponse>>> getGuideStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getGuideStats()));
    }

    /**
     * GET /api/analytics/costs?from=&to=
     * Thống kê chi phí: khách sạn + phương tiện + HDV + lợi nhuận
     */
    @GetMapping("/costs")
    public ResponseEntity<ApiResponse<CostBreakdownResponse>> getCostBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getCostBreakdown(from, to)));
    }

    /**
     * GET /api/analytics/costs/schedule/{scheduleId}
     * Chi phí vận hành của 1 lịch khởi hành cụ thể
     */
    @GetMapping("/costs/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<CostBreakdownResponse>> getCostBySchedule(
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getCostBreakdownBySchedule(scheduleId)));
    }
}