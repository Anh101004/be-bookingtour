package com.bookingtour.review.service.impl;

import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.enums.UserRole;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.exception.AppException;
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
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.security.SecurityUtils;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewResponse reviewResponse;
    private Booking booking;
    private User user;
    private Tour tour;

    @BeforeEach
    void setUp() {

        tour = new Tour();
        tour.setTourId("tour-1");
        tour.setTitle("Da Nang Tour");

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        booking = Booking.builder()
                .bookingId("booking-1")
                .userId("user-1")
                .schedule(schedule)
                .status(BookingStatus.COMPLETED)
                .build();

        review = Review.builder()
                .reviewId("review-1")
                .userId("user-1")
                .bookingId("booking-1")
                .tourId("tour-1")
                .rating(5)
                .comment("Excellent")
                .isHidden(false)
                .build();

        reviewResponse = ReviewResponse.builder()
                .reviewId("review-1")
                .build();

        user = new User();
        user.setUserId("user-1");
        user.setFullName("Nguyen Van A");
        user.setAvatarUrl("avatar.jpg");
        user.setRole(UserRole.CUSTOMER);
        user.setIsActive(true);
    }

    // ==================== PUBLIC ====================

    @Test
    void getVisibleByTourId_Success() {

        when(reviewRepository
                .findAllByTourIdAndIsHiddenFalseOrderByCreatedAtDesc("tour-1"))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));

        List<ReviewResponse> responses =
                reviewService.getVisibleByTourId("tour-1");

        assertEquals(1, responses.size());
    }

    @Test
    void getById_Success() {

        when(reviewRepository.findById("review-1"))
                .thenReturn(Optional.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));

        ReviewResponse response =
                reviewService.getById("review-1");

        assertNotNull(response);
    }

    @Test
    void getById_HiddenReview() {

        review.setIsHidden(true);

        when(reviewRepository.findById("review-1"))
                .thenReturn(Optional.of(review));

        assertThrows(AppException.class,
                () -> reviewService.getById("review-1"));
    }

    // ==================== CREATE ====================

    // FIX 3: Đổi anyString() → any() ở arg thứ 5 của verify(notificationService.send(...))
    //        vì implementation truyền null cho relatedId khi tạo review mới
    @Test
    void create_Success() {

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId("booking-1");
        request.setRating(5);
        request.setComment("Great");

        User admin = new User();
        admin.setUserId("admin-1");
        admin.setRole(UserRole.ADMIN);
        admin.setIsActive(true);

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(reviewRepository.existsByBookingId("booking-1"))
                    .thenReturn(false);

            when(userRepository.findAll())
                    .thenReturn(List.of(admin));

            when(reviewMapper.toResponse(any(Review.class)))
                    .thenReturn(reviewResponse);

            when(userRepository.findById(anyString()))
                    .thenReturn(Optional.of(user));

            when(tourRepository.findById(anyString()))
                    .thenReturn(Optional.of(tour));

            ReviewResponse response =
                    reviewService.create(request);

            assertNotNull(response);

            verify(reviewRepository).save(any(Review.class));

            // any() thay vì anyString() để chấp nhận cả null ở vị trí relatedId
            verify(notificationService, atLeastOnce())
                    .send(
                            anyString(),
                            any(),
                            anyString(),
                            anyString(),
                            any(),      // relatedId có thể null
                            any()
                    );
        }
    }

    @Test
    void create_BookingNotOwner() {

        booking.setUserId("other-user");

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId("booking-1");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            assertThrows(AppException.class,
                    () -> reviewService.create(request));
        }
    }

    @Test
    void create_BookingNotCompleted() {

        booking.setStatus(BookingStatus.CONFIRMED);

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId("booking-1");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            assertThrows(AppException.class,
                    () -> reviewService.create(request));
        }
    }

    @Test
    void create_AlreadyReviewed() {

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId("booking-1");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(reviewRepository.existsByBookingId("booking-1"))
                    .thenReturn(true);

            assertThrows(AppException.class,
                    () -> reviewService.create(request));
        }
    }

    // ==================== UPDATE ====================

    @Test
    void update_Success() {

        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setRating(4);
        request.setComment("Updated");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(reviewRepository.findById("review-1"))
                    .thenReturn(Optional.of(review));

            when(reviewMapper.toResponse(any(Review.class)))
                    .thenReturn(reviewResponse);

            when(userRepository.findById(anyString()))
                    .thenReturn(Optional.of(user));

            when(tourRepository.findById(anyString()))
                    .thenReturn(Optional.of(tour));

            ReviewResponse response =
                    reviewService.update("review-1", request);

            assertNotNull(response);

            assertEquals(4, review.getRating());
            assertEquals("Updated", review.getComment());
        }
    }

    @Test
    void update_NotOwner() {

        review.setUserId("other-user");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(reviewRepository.findById("review-1"))
                    .thenReturn(Optional.of(review));

            assertThrows(AppException.class,
                    () -> reviewService.update(
                            "review-1",
                            new ReviewUpdateRequest()
                    ));
        }
    }

    // ==================== DELETE ====================

    @Test
    void delete_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(reviewRepository.findById("review-1"))
                    .thenReturn(Optional.of(review));

            reviewService.delete("review-1");

            verify(reviewRepository).delete(review);
        }
    }

    @Test
    void delete_NotOwner() {

        review.setUserId("other-user");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(reviewRepository.findById("review-1"))
                    .thenReturn(Optional.of(review));

            assertThrows(AppException.class,
                    () -> reviewService.delete("review-1"));
        }
    }

    // ==================== ADMIN ====================

    @Test
    void getAll_Success() {

        when(reviewRepository.findAll())
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        List<ReviewResponse> responses =
                reviewService.getAll();

        assertEquals(1, responses.size());
    }

    @Test
    void getAllByTourId_Success() {

        when(reviewRepository.findAllByTourIdOrderByCreatedAtDesc("tour-1"))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        List<ReviewResponse> responses =
                reviewService.getAllByTourId("tour-1");

        assertEquals(1, responses.size());
    }

    @Test
    void getByUserId_Success() {

        when(reviewRepository.findAllByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        List<ReviewResponse> responses =
                reviewService.getByUserId("user-1");

        assertEquals(1, responses.size());
    }

    @Test
    void getByIdForAdmin_Success() {

        when(reviewRepository.findById("review-1"))
                .thenReturn(Optional.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        ReviewResponse response =
                reviewService.getByIdForAdmin("review-1");

        assertNotNull(response);
    }

    @Test
    void hide_Success() {

        HideReviewRequest request = new HideReviewRequest();
        request.setHiddenReason("Spam");

        when(reviewRepository.findById("review-1"))
                .thenReturn(Optional.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        ReviewResponse response =
                reviewService.hide("review-1", request);

        assertNotNull(response);

        assertTrue(review.getIsHidden());

        verify(notificationService).send(
                anyString(),
                eq(NotificationType.SYSTEM),
                anyString(),
                anyString(),
                anyString(),
                eq(NotificationRelatedType.REVIEW)
        );
    }

    @Test
    void unhide_Success() {

        review.setIsHidden(true);

        when(reviewRepository.findById("review-1"))
                .thenReturn(Optional.of(review));

        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(reviewResponse);

        when(userRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        when(tourRepository.findById(anyString()))
                .thenReturn(Optional.of(tour));

        ReviewResponse response =
                reviewService.unhide("review-1");

        assertNotNull(response);

        assertFalse(review.getIsHidden());
    }

    @Test
    void reply_Success() {

        AdminReplyRequest request = new AdminReplyRequest();
        request.setAdminReply("Thanks");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            when(reviewRepository.findById("review-1"))
                    .thenReturn(Optional.of(review));

            when(reviewMapper.toResponse(any(Review.class)))
                    .thenReturn(reviewResponse);

            when(userRepository.findById(anyString()))
                    .thenReturn(Optional.of(user));

            when(tourRepository.findById(anyString()))
                    .thenReturn(Optional.of(tour));

            ReviewResponse response =
                    reviewService.reply("review-1", request);

            assertNotNull(response);

            assertEquals("Thanks", review.getAdminReply());
            assertEquals("admin-1", review.getRepliedBy());
            assertNotNull(review.getRepliedAt());

            verify(notificationService).send(
                    anyString(),
                    eq(NotificationType.REVIEW_REPLY),
                    anyString(),
                    anyString(),
                    anyString(),
                    eq(NotificationRelatedType.REVIEW)
            );
        }
    }
}