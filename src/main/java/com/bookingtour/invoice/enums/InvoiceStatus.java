package com.bookingtour.invoice.enums;

public enum InvoiceStatus {
    PENDING,    // Chưa xuất file PDF
    GENERATED,  // Đã xuất PDF
    SENT        // Đã gửi email cho khách
}