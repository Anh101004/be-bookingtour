package com.bookingtour.review.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.review.dto.request.AdminReplyRequest;
import com.bookingtour.review.dto.request.HideReviewRequest;
import com.bookingtour.review.dto.request.ReviewCreateRequest;
import com.bookingtour.review.dto.request.ReviewUpdateRequest;
import com.bookingtour.review.dto.response.ReviewResponse;
import com.bookingtour.review.service.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    // ==================== PUBLIC ====================

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getVisibleByTourId(
            @PathVariable String tourId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getVisibleByTourId(tourId)));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(
            @PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getById(reviewId)));
    }

    // ==================== CUSTOMER ====================

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đánh giá của bạn đã được ghi nhận", response));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật đánh giá thành công",
                reviewService.update(reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công"));
    }

    // ==================== ADMIN ====================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAll()));
    }

    /**
     * GET /api/reviews/{reviewId}/detail
     * Chi tiết 1 đánh giá kể cả bị ẩn (ADMIN)
     * Dùng khi admin nhấn vào thông báo NEW_REVIEW để xem và phản hồi
     */
    @GetMapping("/{reviewId}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> getByIdForAdmin(
            @PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByIdForAdmin(reviewId)));
    }

    @GetMapping("/tour/{tourId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllByTourId(
            @PathVariable String tourId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getAllByTourId(tourId)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByUserId(
            @PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByUserId(userId)));
    }

    @PatchMapping("/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> hide(
            @PathVariable String reviewId,
            @Valid @RequestBody HideReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã ẩn đánh giá",
                reviewService.hide(reviewId, request)));
    }

    @PatchMapping("/{reviewId}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> unhide(
            @PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã hiển thị lại đánh giá",
                reviewService.unhide(reviewId)));
    }

    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> reply(
            @PathVariable String reviewId,
            @Valid @RequestBody AdminReplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Phản hồi đánh giá thành công",
                reviewService.reply(reviewId, request)));
    }
}