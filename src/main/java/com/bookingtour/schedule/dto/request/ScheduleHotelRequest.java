package com.bookingtour.schedule.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleHotelRequest {

    @NotBlank(message = "Mã khách sạn không được để trống")
    private String hotelId;

    /** Null nếu chưa xác định loại phòng */
    private String roomId;

    @NotNull(message = "Ngày check-in không được để trống")
    private LocalDate checkInDate;

    @NotNull(message = "Ngày check-out không được để trống")
    private LocalDate checkOutDate;

    @NotNull(message = "Số phòng không được để trống")
    @Min(value = 1, message = "Số phòng tối thiểu là 1")
    private Integer numRooms;

    /**
     * Nếu null → service tự tính = numRooms × numNights × pricePerNight
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Tổng tiền không được âm")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal totalPrice;

    private Boolean isConfirmed;

    private String notes;
}