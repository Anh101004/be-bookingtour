package com.bookingtour.invoice.service;

import com.bookingtour.invoice.dto.response.InvoiceResponse;
import com.bookingtour.payment.entity.Payment;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface IInvoiceService {

    // ── Tự động — gọi từ PaymentService khi payment → PAID ────
    InvoiceResponse autoCreate(Payment payment);

    // ── Admin ──────────────────────────────────────────────────
    List<InvoiceResponse> getAll();
    List<InvoiceResponse> getByBookingId(String bookingId);
    InvoiceResponse       getById(String invoiceId);
    void                  exportPdf(String invoiceId, HttpServletResponse response);
    void                  resendEmail(String invoiceId);

    // ── Customer ───────────────────────────────────────────────
    List<InvoiceResponse> getMyInvoicesByBooking(String bookingId);
    void                  downloadMyInvoice(String invoiceId, HttpServletResponse response);
}