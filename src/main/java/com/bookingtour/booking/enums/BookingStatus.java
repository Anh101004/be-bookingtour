package com.bookingtour.booking.enums;

public enum BookingStatus {
    PENDING,     // Chờ xác nhận
    DEPOSITED,   // Đã cọc
    CONFIRMED,   // Đã xác nhận (đã thanh toán đủ)
    CANCELLED,   // Đã hủy
    COMPLETED    // Đã hoàn thành tour
}