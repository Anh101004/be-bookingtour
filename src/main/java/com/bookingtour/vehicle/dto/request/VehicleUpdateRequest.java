package com.bookingtour.vehicle.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleUpdateRequest {

    @NotBlank(message = "Tên phương tiện không được để trống")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Loại phương tiện không được để trống")
    private String type;

    @Size(max = 30)
    private String licensePlate;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(1)
    @Max(1000)
    private Integer capacity;

    @Size(max = 100)
    private String brand;

    @Size(max = 100)
    private String model;

    @Min(1900)
    private Integer manufactureYear;

    @Size(max = 50)
    private String color;

    private String features;

    @Size(max = 500)
    private String imageUrl;

    @NotNull(message = "Giá thuê mỗi ngày không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal pricePerDay;

    /** AVAILABLE / IN_USE / MAINTENANCE / RETIRED */
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String notes;
}