package com.bookingtour.payment.dto.request;

import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentCreateRequest {

    @NotBlank(message = "Mã đặt tour không được để trống")
    private String bookingId;

    @NotNull(message = "Loại thanh toán không được để trống")
    private PaymentType paymentType;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền tối thiểu là 1,000 VNĐ")
    private BigDecimal amount;

    private LocalDate dueDate;

    private String transactionId;

    private String paymentNote;
}