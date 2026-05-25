package com.bookingtour.booking.enums;

public enum BookingPaymentStatus {
    UNPAID,       // Chưa thanh toán
    DEPOSITED,    // Đã cọc (30-50%)
    FULLY_PAID,   // Đã thanh toán đủ
    REFUNDED      // Đã hoàn tiền
}