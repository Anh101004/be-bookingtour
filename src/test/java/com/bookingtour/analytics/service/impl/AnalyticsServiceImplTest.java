package com.bookingtour.analytics.service.impl;

import com.bookingtour.analytics.dto.response.BookingStatsResponse;
import com.bookingtour.analytics.dto.response.CostBreakdownResponse;
import com.bookingtour.analytics.dto.response.DashboardResponse;
import com.bookingtour.analytics.dto.response.GuideStatsResponse;
import com.bookingtour.analytics.dto.response.RevenueStatsResponse;
import com.bookingtour.analytics.dto.response.TourPerformanceResponse;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.booking.repository.CancellationRequestRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.guide.repository.TourGuideRepository;
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.repository.PaymentRefundRepository;
import com.bookingtour.payment.repository.PaymentRepository;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.repository.TourScheduleRepository;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CancellationRequestRepository cancellationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentRefundRepository refundRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourGuideRepository guideRepository;

    @Mock
    private TourScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Booking booking;
    private Tour tour;
    private TourSchedule schedule;

    @BeforeEach
    void setUp() {

        tour = new Tour();
        tour.setTourId("tour-1");
        tour.setTitle("Da Nang Tour");
        tour.setDestination("Da Nang");
        tour.setPriceAdult(BigDecimal.valueOf(5000000));

        schedule = new TourSchedule();
        schedule.setScheduleId("schedule-1");
        schedule.setTour(tour);
        schedule.setDepartureDate(LocalDate.now());
        schedule.setStatus(ScheduleStatus.COMPLETED);

        booking = new Booking();
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setPaymentStatus(BookingPaymentStatus.FULLY_PAID);
        booking.setPaidAmount(BigDecimal.valueOf(1000000));
        booking.setTotalAmount(BigDecimal.valueOf(1200000));
        booking.setRemainingAmount(BigDecimal.valueOf(200000));
        booking.setRefundAmount(BigDecimal.ZERO);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setSchedule(schedule);
    }

    @Test
    void testGetBookingStats() {

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        BookingStatsResponse response =
                analyticsService.getBookingStats(null, null);

        assertNotNull(response);
        assertEquals(1, response.getTotalBookings());
        assertEquals(1, response.getCompletedBookings());
        assertEquals(1, response.getFullyPaidBookings());
    }

    @Test
    void testGetBookingStats_WithDifferentStatuses() {

        Booking cancelled = new Booking();
        cancelled.setStatus(BookingStatus.CANCELLED);
        cancelled.setPaymentStatus(BookingPaymentStatus.UNPAID);
        cancelled.setCreatedAt(LocalDateTime.now());

        when(bookingRepository.findAll())
                .thenReturn(List.of(booking, cancelled));

        BookingStatsResponse response =
                analyticsService.getBookingStats(
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1)
                );

        assertNotNull(response);
        assertEquals(2, response.getTotalBookings());
    }

    @Test
    void testGetRevenueStats() {

        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(1000000));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        RevenueStatsResponse response =
                analyticsService.getRevenueStats(null, null);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1200000), response.getTotalRevenue());
        assertEquals(
                0,
                BigDecimal.valueOf(1000000)
                        .compareTo(response.getPaidRevenue())
        );
        assertEquals(
                0,
                BigDecimal.valueOf(1000000)
                        .compareTo(response.getCashRevenue())
        );
    }

    @Test
    void testGetRevenueStats_EmptyPayments() {

        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(paymentRepository.findAll()).thenReturn(List.of());

        RevenueStatsResponse response =
                analyticsService.getRevenueStats(null, null);

        assertNotNull(response);

        assertEquals(
                0,
                BigDecimal.valueOf(1000000)
                        .compareTo(response.getPaidRevenue())
        );
    }

    @Test
    void testGetRevenueStats_WithCardPayment() {

        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setAmount(BigDecimal.valueOf(300000));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        RevenueStatsResponse response =
                analyticsService.getRevenueStats(null, null);

        assertNotNull(response);
    }

    @Test
    void testGetTourPerformance() {

        when(tourRepository.findAll()).thenReturn(List.of(tour));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<TourPerformanceResponse> responses =
                analyticsService.getTourPerformance();

        assertEquals(1, responses.size());
        assertEquals("Da Nang Tour", responses.get(0).getTourTitle());
    }

    @Test
    void testGetTourPerformance_Empty() {

        when(tourRepository.findAll()).thenReturn(List.of());

        List<TourPerformanceResponse> responses =
                analyticsService.getTourPerformance();

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    @Test
    void testGetTourPerformanceById_Success() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        TourPerformanceResponse response =
                analyticsService.getTourPerformanceById("tour-1");

        assertEquals("tour-1", response.getTourId());
    }

    @Test
    void testGetTourPerformanceById_NotFound() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> analyticsService.getTourPerformanceById("tour-1"));
    }

    @Test
    void testGetGuideStats() {

        TourGuide guide = new TourGuide();
        guide.setGuideId("g1");
        guide.setFullName("Guide Test");
        guide.setExperienceYears(5);

        schedule.setGuide(guide);

        when(guideRepository.findAll()).thenReturn(List.of(guide));

        when(scheduleRepository
                .findAllByGuide_GuideIdOrderByDepartureDateAsc("g1"))
                .thenReturn(List.of(schedule));

        List<GuideStatsResponse> responses =
                analyticsService.getGuideStats();

        assertEquals(1, responses.size());
        assertEquals(1, responses.get(0).getCompletedTours());
    }

    @Test
    void testGetGuideStats_EmptySchedules() {

        TourGuide guide = new TourGuide();
        guide.setGuideId("g2");
        guide.setFullName("Guide Empty");

        when(guideRepository.findAll()).thenReturn(List.of(guide));

        when(scheduleRepository
                .findAllByGuide_GuideIdOrderByDepartureDateAsc("g2"))
                .thenReturn(List.of());

        List<GuideStatsResponse> responses =
                analyticsService.getGuideStats();

        assertEquals(1, responses.size());
    }

    @Test
    void testGetDashboard() {

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());

        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(userRepository.count()).thenReturn(1L);
        when(tourRepository.count()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(paymentRepository.findAll()).thenReturn(List.of());

        when(bookingRepository.sumRevenueByDateRange(any(), any()))
                .thenReturn(BigDecimal.valueOf(1000000));

        when(cancellationRepository
                .findAllByStatusOrderByRequestedAtDesc(any()))
                .thenReturn(List.of());

        when(refundRepository
                .findAllByRefundStatusOrderByRequestedAtDesc(any()))
                .thenReturn(List.of());

        DashboardResponse response = analyticsService.getDashboard();

        assertNotNull(response);
        assertEquals(1, response.getTotalUsers());
        assertEquals(1, response.getTotalBookings());
    }

    @Test
    void testGetDashboard_MultipleData() {

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());

        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(500000));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        when(userRepository.count()).thenReturn(2L);
        when(tourRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        when(bookingRepository.sumRevenueByDateRange(any(), any()))
                .thenReturn(BigDecimal.valueOf(5000000));

        when(cancellationRepository
                .findAllByStatusOrderByRequestedAtDesc(any()))
                .thenReturn(List.of());

        when(refundRepository
                .findAllByRefundStatusOrderByRequestedAtDesc(any()))
                .thenReturn(List.of());

        DashboardResponse response = analyticsService.getDashboard();

        assertNotNull(response);
        assertEquals(2, response.getTotalUsers());
        assertEquals(3, response.getTotalTours());
    }

    @Test
    void testGetCostBreakdown_Empty() {

        when(bookingRepository.findAll()).thenReturn(List.of());

        CostBreakdownResponse response =
                analyticsService.getCostBreakdown(null, null);

        assertEquals(BigDecimal.ZERO, response.getTotalRevenue());
        assertEquals(0, response.getTotalSchedulesIncluded());
    }

    @Test
    void testGetCostBreakdown_WithMultipleCases() {

        Booking b1 = new Booking();
        b1.setStatus(BookingStatus.COMPLETED);
        b1.setPaymentStatus(BookingPaymentStatus.FULLY_PAID);
        b1.setTotalAmount(BigDecimal.valueOf(1000));
        b1.setPaidAmount(BigDecimal.valueOf(1000));
        b1.setRemainingAmount(BigDecimal.ZERO);
        b1.setRefundAmount(BigDecimal.ZERO);
        b1.setCreatedAt(LocalDateTime.now());
        b1.setSchedule(schedule);

        Booking b2 = new Booking();
        b2.setStatus(BookingStatus.CANCELLED);
        b2.setPaymentStatus(BookingPaymentStatus.UNPAID);
        b2.setTotalAmount(BigDecimal.valueOf(2000));
        b2.setPaidAmount(BigDecimal.ZERO);
        b2.setRemainingAmount(BigDecimal.valueOf(2000));
        b2.setRefundAmount(BigDecimal.valueOf(500));
        b2.setCreatedAt(LocalDateTime.now());
        b2.setSchedule(schedule);

        when(bookingRepository.findAll())
                .thenReturn(List.of(b1, b2));

        CostBreakdownResponse response =
                analyticsService.getCostBreakdown(
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1)
                );

        assertNotNull(response);

        assertTrue(
                response.getTotalRevenue()
                        .compareTo(BigDecimal.ZERO) >= 0
        );
    }

    @Test
    void testGetCostBreakdown_OutOfRangeBookings() {

        Booking oldBooking = new Booking();
        oldBooking.setCreatedAt(LocalDateTime.now().minusDays(10));
        oldBooking.setSchedule(schedule);

        when(bookingRepository.findAll())
                .thenReturn(List.of(oldBooking));

        CostBreakdownResponse response =
                analyticsService.getCostBreakdown(
                        LocalDate.now().minusDays(1),
                        LocalDate.now()
                );

        assertNotNull(response);
    }

    @Test
    void testGetCostBreakdown_NullSchedule() {

        Booking nullScheduleBooking = new Booking();
        nullScheduleBooking.setCreatedAt(LocalDateTime.now());
        nullScheduleBooking.setSchedule(null);

        when(bookingRepository.findAll())
                .thenReturn(List.of(nullScheduleBooking));

        CostBreakdownResponse response =
                analyticsService.getCostBreakdown(null, null);

        assertNotNull(response);
    }

    @Test
    void testGetCostBreakdown_NullTour() {

        TourSchedule s = new TourSchedule();
        s.setScheduleId("s2");
        s.setTour(null);

        Booking b = new Booking();
        b.setSchedule(s);
        b.setCreatedAt(LocalDateTime.now());

        when(bookingRepository.findAll())
                .thenReturn(List.of(b));

        CostBreakdownResponse response =
                analyticsService.getCostBreakdown(null, null);

        assertNotNull(response);
    }

    @Test
    void testGetCostBreakdownBySchedule_Success() {

        when(scheduleRepository.findById("s1"))
                .thenReturn(Optional.of(schedule));

        Booking b = new Booking();
        b.setSchedule(schedule);
        b.setTotalAmount(BigDecimal.valueOf(1000));
        b.setPaidAmount(BigDecimal.valueOf(1000));
        b.setRefundAmount(BigDecimal.ZERO);
        b.setCreatedAt(LocalDateTime.now());

        when(bookingRepository.findAll())
                .thenReturn(List.of(b));

        CostBreakdownResponse response =
                analyticsService.getCostBreakdownBySchedule("s1");

        assertNotNull(response);
    }

    @Test
    void testGetCostBreakdownBySchedule_NotFound() {

        when(scheduleRepository.findById("s1"))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> analyticsService.getCostBreakdownBySchedule("s1"));
    }

    @Test
    void testGetTopTours() {

        when(tourRepository.findAll()).thenReturn(List.of(tour));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<TourPerformanceResponse> responses =
                analyticsService.getTopTours(5);

        assertNotNull(responses);
    }

    @Test
    void testGetTopTours_Empty() {

        when(tourRepository.findAll()).thenReturn(List.of());

        List<TourPerformanceResponse> responses =
                analyticsService.getTopTours(5);

        assertEquals(0, responses.size());
    }

}