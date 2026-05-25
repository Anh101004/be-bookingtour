package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    // Tổng quan
    private long       totalUsers;
    private long       totalTours;
    private long       totalBookings;
    private BigDecimal totalRevenue;

    // Tháng này
    private long       bookingsThisMonth;
    private BigDecimal revenueThisMonth;
    private long       newUsersThisMonth;

    // Chờ xử lý
    private long       pendingBookings;
    private long       pendingPayments;
    private long       pendingCancellations;
    private long       pendingRefunds;

    // Top 5 tour
    private List<TourPerformanceResponse> topTours;

    // Thống kê chi tiết
    private BookingStatsResponse  bookingStats;
    private RevenueStatsResponse  revenueStats;
    private CostBreakdownResponse costBreakdown;
}