package com.bookingtour.booking.service.impl;

import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.booking.dto.request.BookingCreateRequest;
import com.bookingtour.booking.dto.request.CancellationCreateRequest;
import com.bookingtour.booking.dto.request.CancellationReviewRequest;
import com.bookingtour.booking.dto.response.BookingResponse;
import com.bookingtour.booking.dto.response.CancellationResponse;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.entity.CancellationRequest;
import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.enums.CancellationStatus;
import com.bookingtour.booking.mapper.BookingMapper;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.booking.repository.CancellationRequestRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.service.INotificationService;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.repository.TourScheduleRepository;
import com.bookingtour.security.SecurityUtils;
import com.bookingtour.tour.entity.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CancellationRequestRepository cancellationRepository;

    @Mock
    private TourScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private TourSchedule schedule;
    private Booking booking;

    @BeforeEach
    void setUp() {

        Tour tour = new Tour();
        tour.setTourId("tour-1");
        tour.setTitle("Tour Đà Nẵng");
        tour.setPriceAdult(BigDecimal.valueOf(1000));
        tour.setPriceChild(BigDecimal.valueOf(500));

        schedule = new TourSchedule();
        schedule.setScheduleId("schedule-1");
        schedule.setTour(tour);
        schedule.setStatus(ScheduleStatus.AVAILABLE);

        // FIX NPE
        schedule.setMaxSeats(20);

        schedule.setAvailableSeats(20);
        schedule.setBookedSeats(0);

        schedule.setDepartureDate(LocalDate.now().plusDays(20));

        booking = Booking.builder()
                .bookingId("booking-1")
                .bookingCode("BK001")
                .userId("user-1")
                .schedule(schedule)
                .numAdults(2)
                .numChildren(1)
                .paidAmount(BigDecimal.valueOf(1000))
                .paymentStatus(BookingPaymentStatus.FULLY_PAID)
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    void create_Success() {

        BookingCreateRequest request = new BookingCreateRequest();
        request.setScheduleId("schedule-1");
        request.setCustomerName("Anh");
        request.setCustomerEmail("anh@gmail.com");
        request.setCustomerPhone("0123456789");
        request.setNumAdults(2);
        request.setNumChildren(1);
        request.setDepositPercent(50);

        BookingResponse response = new BookingResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(scheduleRepository.findById("schedule-1"))
                    .thenReturn(Optional.of(schedule));

            when(userRepository.findById("user-1"))
                    .thenReturn(Optional.of(new User()));

            when(bookingRepository.existsByUserIdAndSchedule_ScheduleIdAndStatusNot(
                    anyString(),
                    anyString(),
                    any()
            )).thenReturn(false);

            // FIX bookingId null
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> {
                        Booking saved = invocation.getArgument(0);
                        saved.setBookingId("booking-1");
                        return saved;
                    });

            when(bookingMapper.toResponse(any()))
                    .thenReturn(response);

            BookingResponse result = bookingService.create(request);

            assertNotNull(result);

            verify(bookingRepository).save(any());
            verify(scheduleRepository).save(any());

            verify(notificationService).send(
                    eq("user-1"),
                    eq(NotificationType.BOOKING_CONFIRMED),
                    anyString(),
                    anyString(),
                    eq("booking-1"),
                    eq(NotificationRelatedType.BOOKING)
            );
        }
    }

    @Test
    void create_ScheduleFull_ThrowsException() {

        schedule.setStatus(ScheduleStatus.FULL);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setScheduleId("schedule-1");

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(scheduleRepository.findById("schedule-1"))
                    .thenReturn(Optional.of(schedule));

            assertThrows(
                    AppException.class,
                    () -> bookingService.create(request)
            );
        }
    }

    @Test
    void getMyBookings_Success() {

        BookingResponse response = new BookingResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findAllByUserIdOrderByCreatedAtDesc("user-1"))
                    .thenReturn(List.of(booking));

            when(bookingMapper.toResponse(any()))
                    .thenReturn(response);

            List<BookingResponse> result =
                    bookingService.getMyBookings();

            assertEquals(1, result.size());
        }
    }

    @Test
    void getMyBookingById_Success() {

        BookingResponse response = new BookingResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(bookingMapper.toResponse(any()))
                    .thenReturn(response);

            BookingResponse result =
                    bookingService.getMyBookingById("booking-1");

            assertNotNull(result);
        }
    }

    @Test
    void requestCancellation_Success() {

        CancellationCreateRequest request =
                new CancellationCreateRequest();

        request.setReason("Không đi");

        CancellationResponse response =
                new CancellationResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(cancellationRepository.existsByBookingIdAndStatus(
                    anyString(),
                    any()
            )).thenReturn(false);

            when(bookingMapper.toCancellationResponse(any()))
                    .thenReturn(response);

            CancellationResponse result =
                    bookingService.requestCancellation(
                            "booking-1",
                            request
                    );

            assertNotNull(result);

            verify(cancellationRepository).save(any());
        }
    }

    @Test
    void updateStatus_ToCompleted_Success() {

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(BookingPaymentStatus.FULLY_PAID);

        BookingResponse response = new BookingResponse();

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toResponse(any()))
                .thenReturn(response);

        BookingResponse result =
                bookingService.updateStatus(
                        "booking-1",
                        BookingStatus.COMPLETED
                );

        assertNotNull(result);

        verify(notificationService).sendIfNotExists(
                anyString(),
                eq(NotificationType.REVIEW_REMINDER),
                anyString(),
                anyString(),
                anyString(),
                eq(NotificationRelatedType.TOUR)
        );
    }

    @Test
    void updateStatus_NotFullyPaid_ThrowsException() {

        booking.setPaymentStatus(BookingPaymentStatus.DEPOSITED);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        assertThrows(
                AppException.class,
                () -> bookingService.updateStatus(
                        "booking-1",
                        BookingStatus.COMPLETED
                )
        );
    }

    @Test
    void cancelByAdmin_Success() {

        booking.setPaidAmount(BigDecimal.valueOf(1000));

        BookingResponse response = new BookingResponse();

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toResponse(any()))
                .thenReturn(response);

        BookingResponse result =
                bookingService.cancelByAdmin(
                        "booking-1",
                        "Admin hủy"
                );

        assertNotNull(result);

        verify(notificationService).send(
                anyString(),
                eq(NotificationType.BOOKING_CANCELLED),
                anyString(),
                anyString(),
                anyString(),
                eq(NotificationRelatedType.BOOKING)
        );
    }

    @Test
    void getPendingCancellations_Success() {

        CancellationRequest request =
                CancellationRequest.builder()
                        .requestId("req-1")
                        .bookingId("booking-1")
                        .build();

        CancellationResponse response =
                new CancellationResponse();

        when(cancellationRepository
                .findAllByStatusOrderByRequestedAtDesc(
                        CancellationStatus.PENDING
                ))
                .thenReturn(List.of(request));

        when(bookingMapper.toCancellationResponse(any()))
                .thenReturn(response);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        List<CancellationResponse> result =
                bookingService.getPendingCancellations();

        assertEquals(1, result.size());
    }

    @Test
    void reviewCancellation_Approved_Success() {

        CancellationRequest cancellation =
                CancellationRequest.builder()
                        .requestId("req-1")
                        .bookingId("booking-1")
                        .userId("user-1")
                        .status(CancellationStatus.PENDING)
                        .expectedRefund(BigDecimal.valueOf(500))
                        .reason("Không đi")
                        .build();

        CancellationReviewRequest request =
                new CancellationReviewRequest();

        request.setAdminNote("OK");

        CancellationResponse response =
                new CancellationResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            when(cancellationRepository.findById("req-1"))
                    .thenReturn(Optional.of(cancellation));

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(bookingMapper.toCancellationResponse(any()))
                    .thenReturn(response);

            CancellationResponse result =
                    bookingService.reviewCancellation(
                            "req-1",
                            true,
                            request
                    );

            assertNotNull(result);

            verify(notificationService).send(
                    anyString(),
                    eq(NotificationType.CANCELLATION_APPROVED),
                    anyString(),
                    anyString(),
                    anyString(),
                    eq(NotificationRelatedType.BOOKING)
            );
        }
    }

    @Test
    void reviewCancellation_Rejected_Success() {

        CancellationRequest cancellation =
                CancellationRequest.builder()
                        .requestId("req-1")
                        .bookingId("booking-1")
                        .userId("user-1")
                        .status(CancellationStatus.PENDING)
                        .build();

        CancellationReviewRequest request =
                new CancellationReviewRequest();

        request.setAdminNote("Từ chối");

        CancellationResponse response =
                new CancellationResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            when(cancellationRepository.findById("req-1"))
                    .thenReturn(Optional.of(cancellation));

            when(bookingRepository.findById("booking-1"))
                    .thenReturn(Optional.of(booking));

            when(bookingMapper.toCancellationResponse(any()))
                    .thenReturn(response);

            CancellationResponse result =
                    bookingService.reviewCancellation(
                            "req-1",
                            false,
                            request
                    );

            assertNotNull(result);

            verify(notificationService).send(
                    anyString(),
                    eq(NotificationType.CANCELLATION_REJECTED),
                    anyString(),
                    anyString(),
                    anyString(),
                    eq(NotificationRelatedType.BOOKING)
            );
        }
    }
}