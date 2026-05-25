package com.bookingtour.invoice.enums;

public enum InvoiceType {
    DEPOSIT,    // Hóa đơn đặt cọc 30%
    REMAINING,  // Hóa đơn thanh toán phần còn lại
    FULL,       // Hóa đơn thanh toán toàn bộ 1 lần
    REFUND      // Hóa đơn hoàn tiền
}