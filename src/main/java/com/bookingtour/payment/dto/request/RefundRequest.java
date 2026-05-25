package com.bookingtour.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {

    @NotBlank(message = "Mã đặt tour không được để trống")
    private String bookingId;

    @NotNull(message = "Mã thanh toán không được để trống")
    private String paymentId;

    @NotBlank(message = "Lý do hoàn tiền không được để trống")
    private String refundReason;
}