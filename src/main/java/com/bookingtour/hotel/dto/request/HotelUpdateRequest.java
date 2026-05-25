package com.bookingtour.hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelUpdateRequest {

    @NotBlank(message = "Tên khách sạn không được để trống")
    @Size(max = 255, message = "Tên khách sạn không được quá 255 ký tự")
    private String name;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer starRating;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 100, message = "Tên thành phố không được quá 100 ký tự")
    private String city;

    @Size(max = 100)
    private String province;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(max = 255)
    private String website;

    private String description;

    private String amenities;

    private String checkInTime;

    private String checkOutTime;

    private Double latitude;

    private Double longitude;

    private Boolean isActive;
}