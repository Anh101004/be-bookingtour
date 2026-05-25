package com.bookingtour.review.service.impl;

import com.bookingtour.auth.enums.UserRole;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.service.INotificationService;
import com.bookingtour.review.dto.request.AdminReplyRequest;
import com.bookingtour.review.dto.request.HideReviewRequest;
import com.bookingtour.review.dto.request.ReviewCreateRequest;
import com.bookingtour.review.dto.request.ReviewUpdateRequest;
import com.bookingtour.review.dto.response.ReviewResponse;
import com.bookingtour.review.entity.Review;
import com.bookingtour.review.mapper.ReviewMapper;
import com.bookingtour.review.repository.ReviewRepository;
import com.bookingtour.review.service.IReviewService;
import com.bookingtour.security.SecurityUtils;
import com.bookingtour.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository     reviewRepository;
    private final BookingRepository    bookingRepository;
    private final UserRepository       userRepository;
    private final TourRepository       tourRepository;
    private final ReviewMapper         reviewMapper;
    private final INotificationService notificationService;

    // ==================== PUBLIC ====================

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getVisibleByTourId(String tourId) {
        return reviewRepository
                .findAllByTourIdAndIsHiddenFalseOrderByCreatedAtDesc(tourId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(String reviewId) {
        Review review = findReviewById(reviewId);
        if (Boolean.TRUE.equals(review.getIsHidden())) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return enrichResponse(review);
    }

    // ==================== CUSTOMER ====================

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.REVIEW_NOT_COMPLETED);
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        String tourId    = booking.getSchedule().getTour().getTourId();
        String tourTitle = booking.getSchedule().getTour().getTitle();

        Review review = Review.builder()
                .userId(userId)
                .bookingId(request.getBookingId())
                .tourId(tourId)
                .rating(request.getRating())
                .comment(request.getComment())
                .guideRating(request.getGuideRating())
                .images(request.getImages())
                .isHidden(false)
                .build();

        reviewRepository.save(review);
        reviewRepository.flush();

        // ── Cảm ơn khách đã đánh giá ─────────────────────────────────────────
        notificationService.send(
                userId,
                NotificationType.REVIEW_RECEIVED,
                "Cảm ơn bạn đã đánh giá! 🙏",
                "Cảm ơn bạn đã chia sẻ trải nghiệm về tour \""
                        + tourTitle + "\". "
                        + "Đánh giá của bạn giúp ích rất nhiều cho những du khách khác!",
                review.getReviewId(),
                NotificationRelatedType.REVIEW
        );

        // ── Thông báo tới tất cả admin có đánh giá mới cần phản hồi ──────────
        userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive())
                        && u.getRole() == UserRole.ADMIN)
                .forEach(admin -> notificationService.send(
                        admin.getUserId(),
                        NotificationType.NEW_REVIEW,
                        "Có đánh giá mới cần phản hồi! 📝",
                        "Khách hàng vừa đánh giá tour \""
                                + tourTitle + "\" với "
                                + request.getRating() + " sao. "
                                + "Nhấn vào để xem và phản hồi.",
                        review.getReviewId(),
                        NotificationRelatedType.REVIEW
                ));

        log.info("Tạo đánh giá mới: userId={} tourId={}", userId, tourId);
        return enrichResponse(review);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ReviewResponse update(String reviewId, ReviewUpdateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        Review review = findReviewById(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNER);
        }

        if (request.getRating()      != null) review.setRating(request.getRating());
        if (request.getComment()     != null) review.setComment(request.getComment());
        if (request.getGuideRating() != null) review.setGuideRating(request.getGuideRating());
        if (request.getImages()      != null) review.setImages(request.getImages());

        reviewRepository.save(review);
        log.info("Cập nhật đánh giá: {}", reviewId);
        return enrichResponse(review);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void delete(String reviewId) {
        String userId = SecurityUtils.getCurrentUserId();
        Review review = findReviewById(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNER);
        }

        reviewRepository.delete(review);
        log.info("Xóa đánh giá: {}", reviewId);
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAll()
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllByTourId(String tourId) {
        return reviewRepository.findAllByTourIdOrderByCreatedAtDesc(tourId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByUserId(String userId) {
        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ReviewResponse getByIdForAdmin(String reviewId) {
        // Không check isHidden — admin xem được tất cả
        return enrichResponse(findReviewById(reviewId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReviewResponse hide(String reviewId, HideReviewRequest request) {
        Review review = findReviewById(reviewId);
        review.setIsHidden(true);
        review.setHiddenReason(request.getHiddenReason());
        reviewRepository.save(review);

        notificationService.send(
                review.getUserId(),
                NotificationType.SYSTEM,
                "Đánh giá của bạn đã bị ẩn",
                "Đánh giá của bạn đã bị ẩn do vi phạm: " + request.getHiddenReason(),
                review.getReviewId(),
                NotificationRelatedType.REVIEW
        );

        log.info("Admin ẩn đánh giá: {}", reviewId);
        return enrichResponse(review);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReviewResponse unhide(String reviewId) {
        Review review = findReviewById(reviewId);
        review.setIsHidden(false);
        review.setHiddenReason(null);
        reviewRepository.save(review);

        log.info("Admin bỏ ẩn đánh giá: {}", reviewId);
        return enrichResponse(review);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReviewResponse reply(String reviewId, AdminReplyRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        Review review  = findReviewById(reviewId);

        review.setAdminReply(request.getAdminReply());
        review.setRepliedAt(LocalDateTime.now());
        review.setRepliedBy(adminId);
        reviewRepository.save(review);

        notificationService.send(
                review.getUserId(),
                NotificationType.REVIEW_REPLY,
                "Đánh giá của bạn có phản hồi mới",
                "Ban quản lý đã phản hồi đánh giá tour của bạn.",
                review.getReviewId(),
                NotificationRelatedType.REVIEW
        );

        log.info("Admin phản hồi đánh giá: {}", reviewId);
        return enrichResponse(review);
    }

    // ==================== Private ====================

    private Review findReviewById(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private ReviewResponse enrichResponse(Review review) {
        ReviewResponse response = reviewMapper.toResponse(review);

        userRepository.findById(review.getUserId()).ifPresent(user -> {
            response.setUserFullName(user.getFullName());
            response.setUserAvatarUrl(user.getAvatarUrl());
        });

        if (review.getTourId() != null) {
            tourRepository.findById(review.getTourId())
                    .ifPresent(tour -> response.setTourTitle(tour.getTitle()));
        }

        return response;
    }
}