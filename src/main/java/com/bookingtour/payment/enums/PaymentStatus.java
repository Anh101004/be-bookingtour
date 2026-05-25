package com.bookingtour.payment.enums;

public enum PaymentStatus {
    PENDING,    // Chờ xác nhận
    PAID,       // Đã thanh toán
    FAILED,     // Thất bại
    REFUNDED,   // Đã hoàn tiền
    CANCELLED   // Đã hủy
}