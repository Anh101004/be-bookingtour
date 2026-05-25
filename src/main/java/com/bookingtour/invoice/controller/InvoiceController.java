package com.bookingtour.invoice.controller;

import com.bookingtour.invoice.dto.response.InvoiceResponse;
import com.bookingtour.invoice.service.IInvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final IInvoiceService invoiceService;

    // ── ADMIN ────────────────────────────────────────────────────────

    /** Lấy toàn bộ hóa đơn */
    @GetMapping("/api/admin/invoices")
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    /** Lấy hóa đơn theo booking */
    @GetMapping("/api/admin/invoices/booking/{bookingId}")
    public ResponseEntity<List<InvoiceResponse>> getByBooking(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(invoiceService.getByBookingId(bookingId));
    }

    /** Chi tiết 1 hóa đơn */
    @GetMapping("/api/admin/invoices/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getById(
            @PathVariable String invoiceId) {
        return ResponseEntity.ok(invoiceService.getById(invoiceId));
    }

    /** Admin download PDF */
    @GetMapping("/api/admin/invoices/{invoiceId}/pdf")
    public void exportPdf(@PathVariable String invoiceId,
                          HttpServletResponse response) {
        invoiceService.exportPdf(invoiceId, response);
    }

    /** Gửi lại email cho khách (nếu khách không nhận được) */
    @PostMapping("/api/admin/invoices/{invoiceId}/resend")
    public ResponseEntity<String> resendEmail(@PathVariable String invoiceId) {
        invoiceService.resendEmail(invoiceId);
        return ResponseEntity.ok("Đã gửi lại email hóa đơn thành công");
    }

    // ── CUSTOMER ─────────────────────────────────────────────────────

    /** Khách xem tất cả hóa đơn của booking mình */
    @GetMapping("/api/bookings/{bookingId}/invoices")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(invoiceService.getMyInvoicesByBooking(bookingId));
    }

    /** Khách download PDF hóa đơn */
    @GetMapping("/api/invoices/{invoiceId}/download")
    public void downloadMyInvoice(@PathVariable String invoiceId,
                                  HttpServletResponse response) {
        invoiceService.downloadMyInvoice(invoiceId, response);
    }
}