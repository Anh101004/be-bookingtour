package com.bookingtour.review.service;

import com.bookingtour.review.dto.request.AdminReplyRequest;
import com.bookingtour.review.dto.request.HideReviewRequest;
import com.bookingtour.review.dto.request.ReviewCreateRequest;
import com.bookingtour.review.dto.request.ReviewUpdateRequest;
import com.bookingtour.review.dto.response.ReviewResponse;

import java.util.List;

public interface IReviewService {

    // ===== Public =====
    List<ReviewResponse> getVisibleByTourId(String tourId);
    ReviewResponse getById(String reviewId);

    // ===== Customer =====
    ReviewResponse create(ReviewCreateRequest request);
    ReviewResponse update(String reviewId, ReviewUpdateRequest request);
    void delete(String reviewId);

    // ===== Admin =====
    List<ReviewResponse> getAll();
    List<ReviewResponse> getAllByTourId(String tourId);
    List<ReviewResponse> getByUserId(String userId);
    ReviewResponse getByIdForAdmin(String reviewId);     // ← thêm
    ReviewResponse hide(String reviewId, HideReviewRequest request);
    ReviewResponse unhide(String reviewId);
    ReviewResponse reply(String reviewId, AdminReplyRequest request);
}