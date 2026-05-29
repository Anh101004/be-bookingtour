package com.bookingtour.analytics.service.impl;

import com.bookingtour.analytics.dto.response.*;
import com.bookingtour.analytics.service.IAnalyticsService;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.enums.CancellationStatus;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.booking.repository.CancellationRequestRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.guide.repository.TourGuideRepository;
import com.bookingtour.payment.entity.PaymentRefund;
import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.RefundStatus;
import com.bookingtour.payment.repository.PaymentRefundRepository;
import com.bookingtour.payment.repository.PaymentRepository;
import com.bookingtour.schedule.entity.ScheduleHotel;
import com.bookingtour.schedule.entity.ScheduleVehicle;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.repository.TourScheduleRepository;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements IAnalyticsService {

    private final BookingRepository             bookingRepository;
    private final CancellationRequestRepository cancellationRepository;
    private final PaymentRepository             paymentRepository;
    private final PaymentRefundRepository       refundRepository;
    private final TourRepository                tourRepository;
    private final TourGuideRepository           guideRepository;
    private final TourScheduleRepository        scheduleRepository;
    private final UserRepository                userRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lương HDV mặc định / lịch khởi hành.
     * Chỉ tính cho các lịch có ít nhất 1 booking CONFIRMED/DEPOSITED/COMPLETED.
     */
    private static final BigDecimal GUIDE_SALARY_PER_TOUR = BigDecimal.valueOf(2_000_000);

    // ════════════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now          = LocalDateTime.now();

        List<Booking> allBookings = bookingRepository.findAll();

        long totalUsers    = userRepository.count();
        long totalTours    = tourRepository.count();
        long totalBookings = allBookings.size();

        // ✅ Doanh thu thực = paidAmount (active bookings) - refundedAmount
        BigDecimal grossRevenue = sumRevenue(allBookings);
        BigDecimal totalRefunded = allBookings.stream()
                .map(Booking::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = grossRevenue.subtract(totalRefunded).max(BigDecimal.ZERO);

        BigDecimal revenueThisMonth = bookingRepository.sumRevenueByDateRange(startOfMonth, now);

        long bookingsThisMonth = allBookings.stream()
                .filter(b -> b.getCreatedAt().isAfter(startOfMonth)).count();
        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(startOfMonth)).count();

        long pendingBookings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long pendingPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING).count();
        long pendingCancellations = cancellationRepository
                .findAllByStatusOrderByRequestedAtDesc(CancellationStatus.PENDING).size();
        long pendingRefunds = refundRepository
                .findAllByRefundStatusOrderByRequestedAtDesc(RefundStatus.PENDING).size();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalTours(totalTours)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .bookingsThisMonth(bookingsThisMonth)
                .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
                .newUsersThisMonth(newUsersThisMonth)
                .pendingBookings(pendingBookings)
                .pendingPayments(pendingPayments)
                .pendingCancellations(pendingCancellations)
                .pendingRefunds(pendingRefunds)
                .topTours(getTopTours(5))
                .bookingStats(getBookingStats(null, null))
                .revenueStats(getRevenueStats(null, null))
                .costBreakdown(getCostBreakdown(null, null))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BOOKING STATS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public BookingStatsResponse getBookingStats(LocalDate from, LocalDate to) {
        List<Booking> bookings = filterByDate(from, to);

        Map<BookingStatus, Long> byStatus = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        Map<BookingPaymentStatus, Long> byPay = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getPaymentStatus, Collectors.counting()));

        return BookingStatsResponse.builder()
                .totalBookings(bookings.size())
                .pendingBookings(byStatus.getOrDefault(BookingStatus.PENDING,    0L))
                .depositedBookings(byStatus.getOrDefault(BookingStatus.DEPOSITED, 0L))
                .confirmedBookings(byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L))
                .completedBookings(byStatus.getOrDefault(BookingStatus.COMPLETED, 0L))
                .cancelledBookings(byStatus.getOrDefault(BookingStatus.CANCELLED, 0L))
                .unpaidBookings(byPay.getOrDefault(BookingPaymentStatus.UNPAID,      0L))
                .depositedPayment(byPay.getOrDefault(BookingPaymentStatus.DEPOSITED,  0L))
                .fullyPaidBookings(byPay.getOrDefault(BookingPaymentStatus.FULLY_PAID, 0L))
                .bookingsByMonth(buildMonthlyBookingMap(bookings))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REVENUE STATS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public RevenueStatsResponse getRevenueStats(LocalDate from, LocalDate to) {
        List<Booking> bookings = filterByDate(from, to);

        // ✅ Tiền thực thu từ booking active (CONFIRMED / DEPOSITED / COMPLETED)
        BigDecimal paidRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.DEPOSITED
                        || b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ Tổng tiền đã hoàn trả (refund PROCESSED) — với null-safe
        BigDecimal refundedAmount = bookings.stream()
                .map(Booking::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ Doanh thu thực = tiền thu - tiền hoàn, không để âm
        BigDecimal totalRevenue = paidRevenue.subtract(refundedAmount).max(BigDecimal.ZERO);

        // ✅ Tiền còn chờ thu: chỉ booking chưa hoàn tất, clamp về 0 (không âm)
        BigDecimal pendingRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING
                        || b.getStatus() == BookingStatus.DEPOSITED
                        || b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getRemainingAmount)
                .filter(Objects::nonNull)
                .map(v -> v.max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Doanh thu theo phương thức thanh toán (chỉ payment PAID)
        var payments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID).toList();
        Map<PaymentMethod, BigDecimal> byMethod = new EnumMap<>(PaymentMethod.class);
        payments.forEach(p -> byMethod.merge(p.getPaymentMethod(), p.getAmount(), BigDecimal::add));

        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)           // doanh thu thực (đã trừ hoàn)
                .paidRevenue(paidRevenue)             // tổng đã thu (gross, chưa trừ hoàn)
                .pendingRevenue(pendingRevenue)       // tiền chờ thu (≥ 0)
                .refundedAmount(refundedAmount)       // tổng đã hoàn
                .revenueByMonth(buildMonthlyRevenueMap(bookings))
                .cashRevenue(byMethod.getOrDefault(PaymentMethod.CASH, BigDecimal.ZERO))
                .bankTransferRevenue(byMethod.getOrDefault(PaymentMethod.BANK_TRANSFER, BigDecimal.ZERO))
                .creditCardRevenue(byMethod.getOrDefault(PaymentMethod.CREDIT_CARD, BigDecimal.ZERO))
                .eWalletRevenue(byMethod.getOrDefault(PaymentMethod.E_WALLET, BigDecimal.ZERO))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TOUR PERFORMANCE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<TourPerformanceResponse> getTourPerformance() {
        return tourRepository.findAll().stream()
                .map(this::buildTourPerf)
                .sorted(Comparator.comparing(TourPerformanceResponse::getTotalRevenue).reversed())
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public TourPerformanceResponse getTourPerformanceById(String tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return buildTourPerf(tour);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<TourPerformanceResponse> getTopTours(int limit) {
        return tourRepository.findAll().stream()
                .map(this::buildTourPerf)
                .sorted(Comparator.comparing(TourPerformanceResponse::getTotalRevenue).reversed())
                .limit(limit)
                .toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GUIDE STATS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<GuideStatsResponse> getGuideStats() {
        return guideRepository.findAll().stream().map(guide -> {
            List<TourSchedule> schedules = scheduleRepository
                    .findAllByGuide_GuideIdOrderByDepartureDateAsc(guide.getGuideId());
            long completed = schedules.stream()
                    .filter(s -> s.getStatus() == ScheduleStatus.COMPLETED).count();
            return GuideStatsResponse.builder()
                    .guideId(guide.getGuideId())
                    .fullName(guide.getFullName())
                    .phone(guide.getPhone())
                    .experienceYears(guide.getExperienceYears())
                    .languages(guide.getLanguages())
                    .totalTours((long) schedules.size())
                    .completedTours(completed)
                    .averageRating(guide.getAverageRating())
                    .totalReviews((long) guide.getTotalTours())
                    .build();
        }).sorted(Comparator.comparing(GuideStatsResponse::getCompletedTours).reversed()).toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  COST BREAKDOWN
    //  Tính chi phí vận hành cho các lịch có booking: CONFIRMED/DEPOSITED/COMPLETED.
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public CostBreakdownResponse getCostBreakdown(LocalDate from, LocalDate to) {

        // ── 1. Booking hợp lệ: CONFIRMED / DEPOSITED / COMPLETED trong kỳ ────
        List<Booking> qualifiedBookings = filterQualifiedBookings(from, to);
        if (qualifiedBookings.isEmpty()) {
            return CostBreakdownResponse.builder()
                    .totalOperatingCost(BigDecimal.ZERO)
                    .totalHotelCost(BigDecimal.ZERO)
                    .totalVehicleCost(BigDecimal.ZERO)
                    .totalGuideCost(BigDecimal.ZERO)
                    .totalRevenue(BigDecimal.ZERO)
                    .estimatedProfit(BigDecimal.ZERO)
                    .profitMarginPercent(0.0)
                    .totalSchedulesIncluded(0)
                    .hotelDetails(List.of())
                    .vehicleDetails(List.of())
                    .guideDetails(List.of())
                    .build();
        }

        // ── 2. Nhóm booking theo schedule_id ─────────────────────────────────
        Map<String, List<Booking>> bookingsBySchedule = qualifiedBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getSchedule().getScheduleId()));

        // ── 3. Lấy các TourSchedule tương ứng ────────────────────────────────
        Set<String> scheduleIds = bookingsBySchedule.keySet();
        List<TourSchedule> schedules = scheduleRepository.findAllById(scheduleIds);

        // ── 4. Chi phí KHÁCH SẠN ─────────────────────────────────────────────
        List<CostBreakdownResponse.HotelCostDetail> hotelDetails = new ArrayList<>();
        BigDecimal totalHotelCost = BigDecimal.ZERO;

        for (TourSchedule s : schedules) {
            List<Booking> bks       = bookingsBySchedule.get(s.getScheduleId());
            long          qualCount = bks.size();

            for (ScheduleHotel sh : s.getHotels()) {
                long nights = ChronoUnit.DAYS.between(sh.getCheckInDate(), sh.getCheckOutDate());

                BigDecimal pricePerNight = (sh.getRoom() != null
                        && sh.getRoom().getPricePerNight() != null)
                        ? sh.getRoom().getPricePerNight()
                        : BigDecimal.ZERO;

                BigDecimal cost = pricePerNight
                        .multiply(BigDecimal.valueOf(sh.getNumRooms()))
                        .multiply(BigDecimal.valueOf(nights));

                totalHotelCost = totalHotelCost.add(cost);

                hotelDetails.add(CostBreakdownResponse.HotelCostDetail.builder()
                        .scheduleId(s.getScheduleId())
                        .tourTitle(s.getTour().getTitle())
                        .departureDate(s.getDepartureDate().format(DATE_FMT))
                        .hotelName(sh.getHotel().getName())
                        .hotelCity(sh.getHotel().getCity())
                        .starRating(sh.getHotel().getStarRating())
                        .roomType(sh.getRoom() != null ? sh.getRoom().getRoomType() : "—")
                        .pricePerNight(pricePerNight)
                        .numRooms(sh.getNumRooms())
                        .checkInDate(sh.getCheckInDate().format(DATE_FMT))
                        .checkOutDate(sh.getCheckOutDate().format(DATE_FMT))
                        .nights((int) nights)
                        .totalCost(cost)
                        .isConfirmed(sh.getIsConfirmed())
                        .qualifiedBookings(qualCount)
                        .build());
            }
        }

        // ── 5. Chi phí PHƯƠNG TIỆN ───────────────────────────────────────────
        List<CostBreakdownResponse.VehicleCostDetail> vehicleDetails = new ArrayList<>();
        BigDecimal totalVehicleCost = BigDecimal.ZERO;

        for (TourSchedule s : schedules) {
            List<Booking> bks = bookingsBySchedule.get(s.getScheduleId());

            int actualPassengers = bks.stream()
                    .mapToInt(b -> b.getNumAdults() + b.getNumChildren())
                    .sum();

            for (ScheduleVehicle sv : s.getVehicles()) {
                BigDecimal pricePerPerson = sv.getPricePerPerson() != null
                        ? sv.getPricePerPerson() : BigDecimal.ZERO;

                BigDecimal legCost = pricePerPerson.multiply(BigDecimal.valueOf(actualPassengers));
                totalVehicleCost   = totalVehicleCost.add(legCost);

                vehicleDetails.add(CostBreakdownResponse.VehicleCostDetail.builder()
                        .scheduleId(s.getScheduleId())
                        .tourTitle(s.getTour().getTitle())
                        .departureDate(s.getDepartureDate().format(DATE_FMT))
                        .vehicleName(sv.getVehicle().getName())
                        .vehicleType(sv.getVehicle().getType())
                        .licensePlate(sv.getVehicle().getLicensePlate())
                        .capacity(sv.getVehicle().getCapacity())
                        .legDescription(sv.getLegDescription())
                        .departureTime(sv.getDepartureTime() != null
                                ? sv.getDepartureTime().format(DT_FMT) : "—")
                        .arrivalTime(sv.getArrivalTime() != null
                                ? sv.getArrivalTime().format(DT_FMT) : "—")
                        .pricePerPerson(pricePerPerson)
                        .actualPassengers(actualPassengers)
                        .legCost(legCost)
                        .build());
            }
        }

        // ── 6. Chi phí HƯỚNG DẪN VIÊN ────────────────────────────────────────
        List<CostBreakdownResponse.GuideCostDetail> guideDetails = new ArrayList<>();
        BigDecimal totalGuideCost = BigDecimal.ZERO;

        Map<String, List<TourSchedule>> schedulesByGuide = schedules.stream()
                .filter(s -> s.getGuide() != null)
                .collect(Collectors.groupingBy(s -> s.getGuide().getGuideId()));

        for (Map.Entry<String, List<TourSchedule>> entry : schedulesByGuide.entrySet()) {
            TourGuide          guide              = entry.getValue().get(0).getGuide();
            List<TourSchedule> guideSchedules     = entry.getValue();
            long               qualifiedSchedules = guideSchedules.size();
            long               completedSchedules = guideSchedules.stream()
                    .filter(s -> s.getStatus() == ScheduleStatus.COMPLETED).count();

            BigDecimal estimatedTotal = GUIDE_SALARY_PER_TOUR
                    .multiply(BigDecimal.valueOf(qualifiedSchedules));
            totalGuideCost = totalGuideCost.add(estimatedTotal);

            guideDetails.add(CostBreakdownResponse.GuideCostDetail.builder()
                    .guideId(guide.getGuideId())
                    .guideName(guide.getFullName())
                    .phone(guide.getPhone())
                    .experienceYears(guide.getExperienceYears())
                    .languages(guide.getLanguages())
                    .qualifiedSchedules(qualifiedSchedules)
                    .completedSchedules(completedSchedules)
                    .salaryPerTour(GUIDE_SALARY_PER_TOUR)
                    .estimatedTotal(estimatedTotal)
                    .build());
        }

        // ── 7. Tổng hợp ──────────────────────────────────────────────────────
        BigDecimal totalOperatingCost = totalHotelCost
                .add(totalVehicleCost)
                .add(totalGuideCost);

        // ✅ Doanh thu cho cost breakdown cũng trừ hoàn tiền
        BigDecimal grossRevenue = sumRevenue(qualifiedBookings);
        BigDecimal qualifiedRefunded = qualifiedBookings.stream()
                .map(Booking::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = grossRevenue.subtract(qualifiedRefunded).max(BigDecimal.ZERO);

        BigDecimal estimatedProfit = totalRevenue.subtract(totalOperatingCost);
        double margin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? estimatedProfit
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return CostBreakdownResponse.builder()
                .totalOperatingCost(totalOperatingCost)
                .totalHotelCost(totalHotelCost)
                .totalVehicleCost(totalVehicleCost)
                .totalGuideCost(totalGuideCost)
                .totalRevenue(totalRevenue)
                .estimatedProfit(estimatedProfit)
                .profitMarginPercent(margin)
                .totalSchedulesIncluded((long) schedules.size())
                .hotelDetails(hotelDetails)
                .vehicleDetails(vehicleDetails)
                .guideDetails(guideDetails)
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public CostBreakdownResponse getCostBreakdownBySchedule(String scheduleId) {
        TourSchedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        return getCostBreakdown(s.getDepartureDate(), s.getReturnDate());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REFUND ANALYTICS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public RefundAnalyticsResponse getRefundAnalytics(LocalDate from, LocalDate to) {

        // ── 1. Lấy tất cả refund, lọc theo requestedAt ────────────────────
        List<PaymentRefund> allRefunds = refundRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .filter(r -> {
                    LocalDate d = r.getRequestedAt().toLocalDate();
                    return (from == null || !d.isBefore(from))
                            && (to   == null || !d.isAfter(to));
                })
                .toList();

        if (allRefunds.isEmpty()) {
            return RefundAnalyticsResponse.builder()
                    .totalRefundRequests(0)
                    .processedRefunds(0)
                    .rejectedRefunds(0)
                    .pendingRefunds(0)
                    .totalRefundedAmount(BigDecimal.ZERO)
                    .pendingRefundAmount(BigDecimal.ZERO)
                    .rejectedRefundAmount(BigDecimal.ZERO)
                    .approvalRatePercent(0.0)
                    .refundToRevenueRatePercent(0.0)
                    .countByRefundPercent(Map.of())
                    .amountByRefundPercent(Map.of())
                    .refundByMonth(Map.of())
                    .countByMonth(Map.of())
                    .topRefundCustomers(List.of())
                    .refundDetails(List.of())
                    .build();
        }

        // ── 2. Phân nhóm theo status ──────────────────────────────────────
        Map<RefundStatus, List<PaymentRefund>> byStatus = allRefunds.stream()
                .collect(Collectors.groupingBy(PaymentRefund::getRefundStatus));

        List<PaymentRefund> processed = byStatus.getOrDefault(RefundStatus.PROCESSED, List.of());
        List<PaymentRefund> rejected  = byStatus.getOrDefault(RefundStatus.REJECTED,  List.of());
        List<PaymentRefund> pending   = byStatus.getOrDefault(RefundStatus.PENDING,   List.of());

        BigDecimal totalRefundedAmount = processed.stream()
                .map(PaymentRefund::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRefundAmount = pending.stream()
                .map(PaymentRefund::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rejectedRefundAmount = rejected.stream()
                .map(PaymentRefund::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── 3. Tỷ lệ chấp thuận ──────────────────────────────────────────
        long total = allRefunds.size();
        double approvalRate = total > 0
                ? BigDecimal.valueOf(processed.size())
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        // ── 4. Tỷ lệ hoàn / doanh thu ────────────────────────────────────
        // ✅ Dùng doanh thu thực (đã trừ hoàn) để tính tỷ lệ
        List<Booking> periodBookings = filterByDate(from, to);
        BigDecimal grossRevenue = sumRevenue(periodBookings);
        BigDecimal periodRefunded = periodBookings.stream()
                .map(Booking::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netRevenue = grossRevenue.subtract(periodRefunded).max(BigDecimal.ZERO);

        double refundToRevenueRate = netRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRefundedAmount
                .divide(netRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        // ── 5. Phân bổ theo % chính sách hoàn ───────────────────────────
        Map<String, Long>       countByPct  = new LinkedHashMap<>();
        Map<String, BigDecimal> amountByPct = new LinkedHashMap<>();
        List.of("0%", "30%", "50%", "70%").forEach(k -> {
            countByPct.put(k, 0L);
            amountByPct.put(k, BigDecimal.ZERO);
        });
        allRefunds.forEach(r -> {
            String key = r.getRefundPercent() + "%";
            countByPct.merge(key, 1L, Long::sum);
            amountByPct.merge(key,
                    r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO,
                    BigDecimal::add);
        });

        // ── 6. Phân bổ theo tháng ────────────────────────────────────────
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> refundByMonth = new LinkedHashMap<>();
        Map<String, Long>       countByMonth  = new LinkedHashMap<>();
        YearMonth cur = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            String k = cur.minusMonths(i).format(fmt);
            refundByMonth.put(k, BigDecimal.ZERO);
            countByMonth.put(k, 0L);
        }
        processed.forEach(r -> {
            if (r.getProcessedAt() == null) return;
            String k = r.getProcessedAt().format(fmt);
            refundByMonth.computeIfPresent(k, (key, v) ->
                    v.add(r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO));
            countByMonth.computeIfPresent(k, (key, v) -> v + 1);
        });

        // ── 7. Top khách hoàn tiền nhiều nhất ────────────────────────────
        Map<String, List<PaymentRefund>> byBooking = processed.stream()
                .collect(Collectors.groupingBy(PaymentRefund::getBookingId));

        Map<String, BigDecimal> refundByUser = new HashMap<>();
        Map<String, Long>       countByUser  = new HashMap<>();
        Map<String, String>     nameByUser   = new HashMap<>();
        Map<String, String>     emailByUser  = new HashMap<>();

        byBooking.forEach((bookingId, refunds) ->
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    String uid = booking.getUserId();
                    BigDecimal sum = refunds.stream()
                            .map(PaymentRefund::getRefundAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    refundByUser.merge(uid, sum, BigDecimal::add);
                    countByUser.merge(uid, (long) refunds.size(), Long::sum);
                    userRepository.findById(uid).ifPresent(u -> {
                        nameByUser.put(uid, u.getFullName());
                        emailByUser.put(uid, u.getEmail());
                    });
                }));

        List<RefundAnalyticsResponse.TopRefundCustomer> topCustomers = refundByUser.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> RefundAnalyticsResponse.TopRefundCustomer.builder()
                        .userId(e.getKey())
                        .fullName(nameByUser.getOrDefault(e.getKey(), "—"))
                        .email(emailByUser.getOrDefault(e.getKey(), "—"))
                        .refundCount(countByUser.getOrDefault(e.getKey(), 0L))
                        .totalRefundedAmount(e.getValue())
                        .build())
                .toList();

        // ── 8. Chi tiết từng yêu cầu hoàn tiền ──────────────────────────
        List<RefundAnalyticsResponse.RefundDetail> details = allRefunds.stream()
                .map(r -> {
                    String     bookingCode  = "—";
                    String     userId       = "—";
                    String     customerName = "—";
                    String     tourTitle    = "—";
                    BigDecimal origAmt      = BigDecimal.ZERO;

                    var bookingOpt = bookingRepository.findById(r.getBookingId());
                    if (bookingOpt.isPresent()) {
                        Booking b = bookingOpt.get();
                        bookingCode = b.getBookingCode();
                        userId      = b.getUserId();
                        tourTitle   = b.getSchedule().getTour().getTitle();
                        var userOpt = userRepository.findById(userId);
                        if (userOpt.isPresent()) customerName = userOpt.get().getFullName();
                    }
                    var payOpt = paymentRepository.findById(r.getPaymentId());
                    if (payOpt.isPresent()) origAmt = payOpt.get().getAmount();

                    return RefundAnalyticsResponse.RefundDetail.builder()
                            .refundId(r.getRefundId())
                            .bookingCode(bookingCode)
                            .userId(userId)
                            .customerName(customerName)
                            .tourTitle(tourTitle)
                            .originalPaymentAmount(origAmt)
                            .refundPercent(r.getRefundPercent())
                            .refundAmount(r.getRefundAmount())
                            .daysBeforeTour(r.getDaysBeforeTour())
                            .refundReason(r.getRefundReason())
                            .refundStatus(r.getRefundStatus().name())
                            .requestedAt(r.getRequestedAt().format(DT_FMT))
                            .processedAt(r.getProcessedAt() != null
                                    ? r.getProcessedAt().format(DT_FMT) : "—")
                            .note(r.getNote())
                            .build();
                })
                .toList();

        // ── 9. Trả kết quả ───────────────────────────────────────────────
        return RefundAnalyticsResponse.builder()
                .totalRefundRequests(total)
                .processedRefunds(processed.size())
                .rejectedRefunds(rejected.size())
                .pendingRefunds(pending.size())
                .totalRefundedAmount(totalRefundedAmount)
                .pendingRefundAmount(pendingRefundAmount)
                .rejectedRefundAmount(rejectedRefundAmount)
                .approvalRatePercent(approvalRate)
                .refundToRevenueRatePercent(refundToRevenueRate)
                .countByRefundPercent(countByPct)
                .amountByRefundPercent(amountByPct)
                .refundByMonth(refundByMonth)
                .countByMonth(countByMonth)
                .topRefundCustomers(topCustomers)
                .refundDetails(details)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Lọc booking theo ngày tạo (created_at).
     * Không áp dụng điều kiện status — dùng chung cho booking/revenue stats.
     */
    private List<Booking> filterByDate(LocalDate from, LocalDate to) {
        return bookingRepository.findAll().stream().filter(b -> {
            LocalDate c = b.getCreatedAt().toLocalDate();
            return (from == null || !c.isBefore(from)) && (to == null || !c.isAfter(to));
        }).toList();
    }

    /**
     * Lọc booking đủ điều kiện tính chi phí vận hành.
     * Bao gồm: CONFIRMED, DEPOSITED, COMPLETED — loại trừ PENDING và CANCELLED.
     * Lọc theo departure_date của schedule.
     */
    private List<Booking> filterQualifiedBookings(LocalDate from, LocalDate to) {
        return bookingRepository.findAll().stream().filter(b -> {
            BookingStatus st = b.getStatus();
            boolean statusOk = st == BookingStatus.CONFIRMED
                    || st == BookingStatus.DEPOSITED
                    || st == BookingStatus.COMPLETED;
            if (!statusOk) return false;

            LocalDate departureDate = b.getSchedule().getDepartureDate();
            boolean afterFrom = from == null || !departureDate.isBefore(from);
            boolean beforeTo  = to   == null || !departureDate.isAfter(to);
            return afterFrom && beforeTo;
        }).toList();
    }

    /**
     * Tổng doanh thu gross = paidAmount của booking CONFIRMED/DEPOSITED/COMPLETED.
     * Lưu ý: đây là gross, chưa trừ refund. Việc trừ hoàn làm ở tầng gọi.
     */
    private BigDecimal sumRevenue(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.DEPOSITED
                        || b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TourPerformanceResponse buildTourPerf(Tour tour) {
        List<Booking> bks = bookingRepository.findAll().stream()
                .filter(b -> b.getSchedule().getTour().getTourId().equals(tour.getTourId()))
                .toList();
        long total     = bks.size();
        long completed = bks.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelled = bks.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        // ✅ Doanh thu tour = paidAmount - refundAmount (net)
        BigDecimal grossRevenue = bks.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.DEPOSITED
                        || b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tourRefunded = bks.stream()
                .map(Booking::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal revenue = grossRevenue.subtract(tourRefunded).max(BigDecimal.ZERO);

        double rate = total > 0
                ? BigDecimal.valueOf(cancelled)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return TourPerformanceResponse.builder()
                .tourId(tour.getTourId())
                .tourTitle(tour.getTitle())
                .destination(tour.getDestination())
                .priceAdult(tour.getPriceAdult())
                .totalBookings(total)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(revenue)
                .averageRating(tour.getAverageRating())
                .ratingCount(tour.getRatingCount())
                .viewCount(tour.getViewCount())
                .cancellationRate(rate)
                .build();
    }

    private Map<String, Long> buildMonthlyBookingMap(List<Booking> bookings) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> result = new LinkedHashMap<>();
        YearMonth cur = YearMonth.now();
        for (int i = 11; i >= 0; i--) result.put(cur.minusMonths(i).format(fmt), 0L);
        bookings.forEach(b ->
                result.computeIfPresent(b.getCreatedAt().format(fmt), (k, v) -> v + 1));
        return result;
    }

    private Map<String, BigDecimal> buildMonthlyRevenueMap(List<Booking> bookings) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        YearMonth cur = YearMonth.now();
        for (int i = 11; i >= 0; i--) result.put(cur.minusMonths(i).format(fmt), BigDecimal.ZERO);
        bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.DEPOSITED
                        || b.getStatus() == BookingStatus.COMPLETED)
                .forEach(b -> result.computeIfPresent(
                        b.getCreatedAt().format(fmt),
                        (k, v) -> v.add(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO)));
        return result;
    }
}