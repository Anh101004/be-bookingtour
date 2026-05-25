package com.bookingtour.schedule.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ScheduleResponse {

    // ── Lịch khởi hành ───────────────────────────────────────────
    private String    scheduleId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer   maxSeats;
    private Integer   bookedSeats;
    private Integer   availableSeats;
    private String    status;
    private String    statusLabel;
    private String    notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Tour ─────────────────────────────────────────────────────
    private String     tourId;
    private String     tourTitle;
    private String     destination;
    private Integer    durationDays;
    private Integer    durationNights;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private String     tourFeaturedImage;

    // ── Lịch trình theo ngày ─────────────────────────────────────
    private List<ItineraryDayResponse> itineraries;

    // ── Hướng dẫn viên ───────────────────────────────────────────
    private String guideId;
    private String guideName;
    private String guidePhone;
    private String guideLanguages;
    private String guideAvatarUrl;

    // ── Phương tiện ──────────────────────────────────────────────
    private List<ScheduleVehicleResponse> vehicles;
    private BigDecimal totalTransportCostPerPerson;

    // ── Khách sạn ────────────────────────────────────────────────
    private List<ScheduleHotelResponse>  hotels;
    private BigDecimal totalHotelCost;
    private Long       pendingConfirmCount;

    // ── Inner: lịch trình 1 ngày ─────────────────────────────────
    @Data
    @Builder
    public static class ItineraryDayResponse {
        private String  itineraryId;
        private Integer dayNumber;
        private String  title;
        private String  description;
        private String  activities;
        private String  meals;          // "Sáng, Trưa, Tối"
        private String  accommodation;
        private String  imageUrl;
    }
}