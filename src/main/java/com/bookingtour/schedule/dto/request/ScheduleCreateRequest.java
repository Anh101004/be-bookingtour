package com.bookingtour.schedule.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleCreateRequest {

    @NotBlank(message = "Mã tour không được để trống")
    private String tourId;

    /**
     * Mã HDV — có thể null, admin assign sau.
     * Khi có guideId sẽ tự tạo guide_schedule đi kèm.
     */
    private String guideId;

    @NotNull(message = "Ngày khởi hành không được để trống")
    private LocalDate departureDate;

    @NotNull(message = "Ngày về không được để trống")
    private LocalDate returnDate;

    @NotNull(message = "Số chỗ tối đa không được để trống")
    @Min(value = 1, message = "Số chỗ tối thiểu là 1")
    private Integer maxSeats;

    private String notes;

    /**
     * Danh sách chặng xe — tuỳ chọn, có thể bổ sung sau.
     * Nếu truyền lên sẽ được lưu cùng 1 transaction với lịch.
     */
    @Valid
    private List<ScheduleVehicleRequest> vehicles = new ArrayList<>();

    /**
     * Danh sách khách sạn — tuỳ chọn, có thể bổ sung sau.
     * Nếu truyền lên sẽ được lưu cùng 1 transaction với lịch.
     */
    @Valid
    private List<ScheduleHotelRequest> hotels = new ArrayList<>();
}