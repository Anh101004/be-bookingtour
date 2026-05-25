package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CostBreakdownResponse {

    // ── Tổng chi phí vận hành (chỉ tính từ booking COMPLETED + FULLY_PAID) ──
    private BigDecimal totalOperatingCost;
    private BigDecimal totalHotelCost;
    private BigDecimal totalVehicleCost;   // Tính từ price_per_person × số khách thực tế
    private BigDecimal totalGuideCost;

    // ── Doanh thu & lợi nhuận ──
    private BigDecimal totalRevenue;       // Tổng paid_amount từ booking COMPLETED + FULLY_PAID
    private BigDecimal estimatedProfit;
    private Double     profitMarginPercent;

    // ── Số lượng schedule được tính ──
    private long totalSchedulesIncluded;   // Số lịch khởi hành có booking hợp lệ

    // ── Chi tiết từng loại ──
    private List<HotelCostDetail>   hotelDetails;
    private List<VehicleCostDetail> vehicleDetails;
    private List<GuideCostDetail>   guideDetails;

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER: Chi phí khách sạn
    // ─────────────────────────────────────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HotelCostDetail {
        private String     scheduleId;
        private String     tourTitle;
        private String     departureDate;
        private String     hotelName;
        private String     hotelCity;
        private Integer    starRating;
        private String     roomType;
        /** Giá phòng / đêm lấy từ hotel_rooms.price_per_night */
        private BigDecimal pricePerNight;
        private Integer    numRooms;
        private String     checkInDate;
        private String     checkOutDate;
        private Integer    nights;
        private BigDecimal totalCost;
        private Boolean    isConfirmed;
        /** Số booking COMPLETED + FULLY_PAID gắn với schedule này */
        private long       qualifiedBookings;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER: Chi phí phương tiện
    //  Được tính = price_per_person × tổng số hành khách thực tế
    //  (num_adults + num_children) từ booking COMPLETED + FULLY_PAID
    // ─────────────────────────────────────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VehicleCostDetail {
        private String     scheduleId;
        private String     tourTitle;
        private String     departureDate;
        private String     vehicleName;
        private String     vehicleType;
        private String     licensePlate;
        private Integer    capacity;
        private String     legDescription;
        private String     departureTime;
        private String     arrivalTime;

        /** Giá vận chuyển / người / chặng (từ schedule_vehicles.price_per_person) */
        private BigDecimal pricePerPerson;

        /** Tổng hành khách thực tế (num_adults + num_children) đã COMPLETED + FULLY_PAID */
        private int        actualPassengers;

        /** Chi phí chặng = pricePerPerson × actualPassengers */
        private BigDecimal legCost;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER: Chi phí hướng dẫn viên
    // ─────────────────────────────────────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GuideCostDetail {
        private String     guideId;
        private String     guideName;
        private String     phone;
        private Integer    experienceYears;
        private String     languages;
        /** Số lịch khởi hành có ít nhất 1 booking COMPLETED + FULLY_PAID */
        private long       qualifiedSchedules;
        private long       completedSchedules;
        private BigDecimal salaryPerTour;
        /** Chi phí ước tính = salaryPerTour × qualifiedSchedules */
        private BigDecimal estimatedTotal;
    }
}