package com.bookingtour.analytics.service;

import com.bookingtour.analytics.dto.response.*;
import java.time.LocalDate;
import java.util.List;

public interface IAnalyticsService {

    DashboardResponse getDashboard();

    BookingStatsResponse getBookingStats(LocalDate from, LocalDate to);

    RevenueStatsResponse getRevenueStats(LocalDate from, LocalDate to);

    // Tour performance
    List<TourPerformanceResponse> getTourPerformance();
    TourPerformanceResponse getTourPerformanceById(String tourId);
    List<TourPerformanceResponse> getTopTours(int limit);

    // Guide stats
    List<GuideStatsResponse> getGuideStats();

    // Chi phí vận hành (mới)
    CostBreakdownResponse getCostBreakdown(LocalDate from, LocalDate to);
    CostBreakdownResponse getCostBreakdownBySchedule(String scheduleId);
}