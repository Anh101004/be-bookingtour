package com.bookingtour.schedule.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduleVehicleRequest {

    @NotBlank(message = "Mã phương tiện không được để trống")
    private String vehicleId;

    @NotBlank(message = "Mô tả chặng không được để trống")
    @Size(max = 255)
    private String legDescription;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vận chuyển không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá không được âm")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal pricePerPerson;

    private String notes;
}