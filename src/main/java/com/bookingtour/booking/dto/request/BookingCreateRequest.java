package com.bookingtour.booking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingCreateRequest {

    @NotBlank(message = "Lịch khởi hành không được để trống")
    private String scheduleId;

    @NotBlank(message = "Họ tên khách hàng không được để trống")
    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    private String customerName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String customerEmail;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @NotNull(message = "Số người lớn không được để trống")
    @Min(value = 1, message = "Phải có ít nhất 1 người lớn")
    private Integer numAdults;

    @Min(value = 0, message = "Số trẻ em không được âm")
    private Integer numChildren = 0;

    @NotNull(message = "Phần trăm đặt cọc không được để trống")
    private Integer depositPercent = 30; // 30 hoặc 50

    private String notes;
}