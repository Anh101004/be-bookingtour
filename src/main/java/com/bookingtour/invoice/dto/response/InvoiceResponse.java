package com.bookingtour.invoice.dto.response;

import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.enums.InvoiceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InvoiceResponse {

    private String invoiceId;
    private String invoiceNumber;
    private String bookingId;
    private String paymentId;

    private InvoiceType   invoiceType;
    private InvoiceStatus status;

    // Thông tin khách
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Thông tin tour
    private String    tourTitle;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String    departureLocation;
    private String    destination;
    private String    guideName;

    // Số lượng & đơn giá
    private Integer    numAdults;
    private Integer    numChildren;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;

    // Tiền
    private BigDecimal totalAmount;
    private BigDecimal invoiceAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate  dueDate;

    // File & meta
    private String        fileUrl;
    private String        notes;
    private LocalDateTime issuedAt;
    private LocalDateTime sentAt;
}