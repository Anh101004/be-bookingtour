package com.bookingtour.booking.controller;

import com.bookingtour.booking.dto.request.BookingCreateRequest;
import com.bookingtour.booking.dto.request.CancellationCreateRequest;
import com.bookingtour.booking.dto.request.CancellationReviewRequest;
import com.bookingtour.booking.dto.response.BookingResponse;
import com.bookingtour.booking.dto.response.CancellationResponse;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.service.IBookingService;
import com.bookingtour.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    // ==================== CUSTOMER ====================

    /**
     * POST /api/bookings
     * Đặt tour mới
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody BookingCreateRequest request) {
        BookingResponse response = bookingService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt tour thành công", response));
    }

    /**
     * GET /api/bookings/my
     * Danh sách booking của user đang đăng nhập
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getMyBookings()));
    }

    /**
     * GET /api/bookings/my/{bookingId}
     * Chi tiết 1 booking của user đang đăng nhập
     */
    @GetMapping("/my/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getMyBookingById(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getMyBookingById(bookingId)));
    }

    /**
     * POST /api/bookings/{bookingId}/cancel-request
     * Customer gửi yêu cầu hủy tour
     */
    @PostMapping("/{bookingId}/cancel-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CancellationResponse>> requestCancellation(
            @PathVariable String bookingId,
            @Valid @RequestBody CancellationCreateRequest request) {
        CancellationResponse response = bookingService.requestCancellation(
                bookingId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Gửi yêu cầu hủy thành công, chờ admin xác nhận", response));
    }

    // ==================== ADMIN ====================

    /**
     * GET /api/bookings
     * Tất cả booking (ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAll()));
    }

    /**
     * GET /api/bookings/status/{status}
     * Lọc booking theo status (ADMIN)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getByStatus(
            @PathVariable BookingStatus status) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getByStatus(status)));
    }

    /**
     * GET /api/bookings/schedule/{scheduleId}
     * Booking theo lịch khởi hành (ADMIN)
     */
    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getByScheduleId(
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getByScheduleId(scheduleId)));
    }

    /**
     * GET /api/bookings/user/{userId}
     * Booking của 1 user (ADMIN)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getByUserId(
            @PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getByUserId(userId)));
    }

    /**
     * GET /api/bookings/{bookingId}
     * Chi tiết booking (ADMIN)
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getById(bookingId)));
    }

    /**
     * PATCH /api/bookings/{bookingId}/status?status=COMPLETED
     * Cập nhật trạng thái booking (ADMIN)
     */
    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(
            @PathVariable String bookingId,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái thành công",
                bookingService.updateStatus(bookingId, status)));
    }

    /**
     * PATCH /api/bookings/{bookingId}/cancel
     * Admin hủy booking trực tiếp
     */
    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelByAdmin(
            @PathVariable String bookingId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã hủy booking thành công",
                bookingService.cancelByAdmin(bookingId, reason)));
    }

    // ==================== CANCELLATION ====================

    /**
     * GET /api/bookings/cancellations/pending
     * Danh sách yêu cầu hủy chờ xử lý (ADMIN)
     */
    @GetMapping("/cancellations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CancellationResponse>>> getPendingCancellations() {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getPendingCancellations()));
    }

    /**
     * PATCH /api/bookings/cancellations/{requestId}/review?approved=true
     * Admin duyệt hoặc từ chối yêu cầu hủy
     */
    @PatchMapping("/cancellations/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CancellationResponse>> reviewCancellation(
            @PathVariable String requestId,
            @RequestParam boolean approved,
            @RequestBody(required = false) CancellationReviewRequest request) {
        if (request == null) request = new CancellationReviewRequest();
        CancellationResponse response = bookingService.reviewCancellation(
                requestId, approved, request);
        String msg = approved ? "Đã duyệt yêu cầu hủy" : "Đã từ chối yêu cầu hủy";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}