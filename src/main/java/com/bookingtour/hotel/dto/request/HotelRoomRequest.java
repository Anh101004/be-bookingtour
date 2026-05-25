package com.bookingtour.hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HotelRoomRequest {

    @NotBlank(message = "Loại phòng không được để trống")
    @Size(max = 100, message = "Loại phòng không được quá 100 ký tự")
    private String roomType;

    @Size(max = 100, message = "Loại giường không được quá 100 ký tự")
    private String bedType;

    @Min(value = 1, message = "Sức chứa tối thiểu là 1 người")
    @Max(value = 10, message = "Sức chứa tối đa là 10 người")
    private Integer capacity = 2;

    @Min(value = 1, message = "Số phòng tối thiểu là 1")
    private Integer totalRooms = 1;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phòng phải lớn hơn 0")
    private BigDecimal pricePerNight;

    @DecimalMin(value = "0.0", message = "Diện tích phải lớn hơn 0")
    private BigDecimal areaSqm;

    private String features;

    private String imageUrl;
}