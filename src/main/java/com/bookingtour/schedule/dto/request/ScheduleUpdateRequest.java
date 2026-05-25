package com.bookingtour.schedule.dto.request;

import com.bookingtour.schedule.enums.ScheduleStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleUpdateRequest {

    /**
     * guideId = null      → giữ nguyên HDV cũ
     * guideId = ""        → bỏ HDV (set null)
     * guideId = "abc..."  → thay HDV mới
     */
    private String guideId;

    @NotNull(message = "Ngày khởi hành không được để trống")
    private LocalDate departureDate;

    @NotNull(message = "Ngày về không được để trống")
    private LocalDate returnDate;

    @NotNull(message = "Số chỗ tối đa không được để trống")
    @Min(value = 1, message = "Số chỗ tối thiểu là 1")
    private Integer maxSeats;

    private ScheduleStatus status;

    private String notes;
}