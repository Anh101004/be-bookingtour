package com.bookingtour.vehicle.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleCreateRequest {

    @NotBlank(message = "Tên phương tiện không được để trống")
    @Size(max = 255, message = "Tên phương tiện tối đa 255 ký tự")
    private String name;

    /**
     * BUS / VAN / BOAT / AIRPLANE / TRAIN
     */
    @NotBlank(message = "Loại phương tiện không được để trống")
    private String type;

    @Size(max = 30, message = "Biển số tối đa 30 ký tự")
    private String licensePlate;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1,    message = "Sức chứa tối thiểu 1 chỗ")
    @Max(value = 1000, message = "Sức chứa tối đa 1000 chỗ")
    private Integer capacity;

    @Size(max = 100)
    private String brand;

    @Size(max = 100)
    private String model;

    @Min(value = 1900, message = "Năm sản xuất không hợp lệ")
    private Integer manufactureYear;

    @Size(max = 50)
    private String color;

    /** Điều hòa, WiFi, toilet, USB... */
    private String features;

    @Size(max = 500)
    private String imageUrl;

    /**
     * Giá thuê phương tiện mỗi ngày (VNĐ).
     */
    @NotNull(message = "Giá thuê mỗi ngày không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")
    @Digits(integer = 12, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal pricePerDay;

    private String notes;
}