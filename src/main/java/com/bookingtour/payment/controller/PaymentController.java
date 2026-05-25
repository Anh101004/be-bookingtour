package com.bookingtour.payment.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.payment.dto.request.PaymentConfirmRequest;
import com.bookingtour.payment.dto.request.PaymentCreateRequest;
import com.bookingtour.payment.dto.request.RefundRequest;
import com.bookingtour.payment.dto.response.PaymentResponse;
import com.bookingtour.payment.dto.response.PaymentSummaryResponse;
import com.bookingtour.payment.dto.response.RefundResponse;
import com.bookingtour.payment.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    // ==================== QUERY ====================

    /**
     * GET /api/payments/booking/{bookingId}/summary
     * Tổng quan thanh toán của 1 booking
     */
    @GetMapping("/booking/{bookingId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getSummary(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getSummaryByBookingId(bookingId)));
    }

    /**
     * GET /api/payments/booking/{bookingId}
     * Danh sách các lần thanh toán của booking
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByBookingId(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByBookingId(bookingId)));
    }

    /**
     * GET /api/payments/{paymentId}
     * Chi tiết 1 lần thanh toán
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getById(paymentId)));
    }

    // ==================== CUSTOMER ====================

    /**
     * POST /api/payments
     * Customer tạo yêu cầu thanh toán → status PENDING, chờ Admin xác nhận
     *
     * Body:
     * {
     *   "bookingId": "...",
     *   "paymentType": "DEPOSIT | REMAINING | FULL",
     *   "paymentMethod": "CASH | BANK_TRANSFER | CREDIT_CARD | E_WALLET",
     *   "amount": 3660000,
     *   "transactionId": "TXN123" (tuỳ chọn),
     *   "paymentNote": "Chuyển khoản qua MB Bank" (tuỳ chọn)
     * }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Tạo yêu cầu thanh toán thành công, vui lòng chờ xác nhận",
                        response));
    }

    // ==================== ADMIN ====================

    /**
     * GET /api/payments
     * Tất cả thanh toán (ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAll()));
    }

    /**
     * GET /api/payments/pending
     * Danh sách thanh toán chờ xác nhận (ADMIN)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPending()));
    }

    /**
     * PATCH /api/payments/{paymentId}/confirm
     * Admin xác nhận đã nhận tiền → cập nhật booking tự động
     *
     * Body (tuỳ chọn):
     * {
     *   "transactionId": "TXN123",
     *   "paymentNote": "Đã nhận qua MB Bank"
     * }
     */
    @PatchMapping("/{paymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable String paymentId,
            @RequestBody(required = false) PaymentConfirmRequest request) {
        if (request == null) request = new PaymentConfirmRequest();
        return ResponseEntity.ok(ApiResponse.success(
                "Xác nhận thanh toán thành công",
                paymentService.confirmPayment(paymentId, request)));
    }

    /**
     * PATCH /api/payments/{paymentId}/cancel
     * Admin hủy thanh toán đang pending
     */
    @PatchMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã hủy thanh toán",
                paymentService.cancelPayment(paymentId)));
    }

    // ==================== REFUND ====================

    /**
     * POST /api/payments/refunds
     * Admin tạo yêu cầu hoàn tiền (tự tính % theo chính sách)
     *
     * Body:
     * {
     *   "bookingId": "...",
     *   "paymentId": "...",
     *   "refundReason": "Khách hủy do bệnh"
     * }
     *
     * Chính sách hoàn tiền:
     *   > 15 ngày trước tour → hoàn 70%
     *   7–15 ngày            → hoàn 50%
     *   3–7 ngày             → hoàn 30%
     *   < 3 ngày             → không hoàn (0%)
     */
    @PostMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
            @Valid @RequestBody RefundRequest request) {
        RefundResponse response = paymentService.createRefund(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu hoàn tiền thành công", response));
    }

    /**
     * PATCH /api/payments/refunds/{refundId}/process?approved=true&note=...
     * Admin duyệt hoặc từ chối hoàn tiền
     */
    @PatchMapping("/refunds/{refundId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable String refundId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String note) {
        RefundResponse response = paymentService.processRefund(refundId, approved, note);
        String msg = approved ? "Đã xác nhận hoàn tiền thành công"
                : "Đã từ chối yêu cầu hoàn tiền";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    /**
     * GET /api/payments/refunds/pending
     * Danh sách yêu cầu hoàn tiền chờ xử lý (ADMIN)
     */
    @GetMapping("/refunds/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPendingRefunds() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPendingRefunds()));
    }

    /**
     * GET /api/payments/refunds/booking/{bookingId}
     * Lịch sử hoàn tiền của 1 booking
     */
    @GetMapping("/refunds/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsByBookingId(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getRefundsByBookingId(bookingId)));
    }
}