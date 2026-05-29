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
import com.bookingtour.booking.service.IBookingService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.service.INotificationService;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.repository.TourScheduleRepository;
import com.bookingtour.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final CancellationRequestRepository cancellationRepository;
    private final TourScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final INotificationService notificationService;

    /**
     * Counter phụ tránh trùng booking code trong cùng giây
     */
    private static final AtomicInteger CODE_SEQ = new AtomicInteger(0);

    // ════════════════════════════════════════════════════════════════════════
    //  CUSTOMER
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        TourSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getStatus() == ScheduleStatus.FULL) {
            throw new AppException(ErrorCode.BOOKING_SCHEDULE_FULL);
        }
        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new AppException(ErrorCode.SCHEDULE_CANCELLED);
        }
        if (bookingRepository.existsByUserIdAndSchedule_ScheduleIdAndStatusNot(
                userId, request.getScheduleId(), BookingStatus.CANCELLED)) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }

        int totalPeople = request.getNumAdults() + request.getNumChildren();
        if (schedule.getAvailableSeats() < totalPeople) {
            throw new AppException(ErrorCode.BOOKING_SCHEDULE_FULL,
                    "Lịch khởi hành chỉ còn " + schedule.getAvailableSeats() + " chỗ trống");
        }

        int depositPercent = request.getDepositPercent();
        if (depositPercent != 30 && depositPercent != 50) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Phần trăm đặt cọc phải là 30% hoặc 50%");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BigDecimal priceAdult = schedule.getTour().getPriceAdult();
        BigDecimal priceChild = schedule.getTour().getPriceChild();
        BigDecimal totalAmount = priceAdult.multiply(BigDecimal.valueOf(request.getNumAdults()))
                .add(priceChild.multiply(BigDecimal.valueOf(request.getNumChildren())));
        BigDecimal depositAmount = totalAmount
                .multiply(BigDecimal.valueOf(depositPercent))
                .divide(BigDecimal.valueOf(100));

        Booking booking = Booking.builder()
                .userId(userId)
                .schedule(schedule)
                .bookingCode(generateBookingCode())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .numAdults(request.getNumAdults())
                .numChildren(request.getNumChildren())
                .totalAmount(totalAmount)
                .depositAmount(depositAmount)
                .depositPercent(depositPercent)
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(totalAmount)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .status(BookingStatus.PENDING)
                .notes(request.getNotes())
                .build();

        bookingRepository.save(booking);

        schedule.setBookedSeats(schedule.getBookedSeats() + totalPeople);
        schedule.recalculate();
        scheduleRepository.save(schedule);

        notificationService.send(
                userId,
                NotificationType.BOOKING_CONFIRMED,
                "Đặt tour thành công!",
                "Đơn " + booking.getBookingCode() + " đã được tạo. "
                        + "Vui lòng thanh toán cọc " + formatAmount(depositAmount)
                        + " VNĐ trước " + booking.getDueDate() + ".",
                booking.getBookingId(),
                NotificationRelatedType.BOOKING
        );

        log.info("Tạo booking: {} userId={}", booking.getBookingCode(), userId);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        String userId = SecurityUtils.getCurrentUserId();
        return bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(bookingMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public BookingResponse getMyBookingById(String bookingId) {
        String userId = SecurityUtils.getCurrentUserId();
        Booking booking = findBookingById(bookingId);
        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return bookingMapper.toResponse(booking);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CancellationResponse requestCancellation(String bookingId,
                                                    CancellationCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        Booking booking = findBookingById(bookingId);

        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANCELLED);
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_COMPLETED);
        }
        if (cancellationRepository.existsByBookingIdAndStatus(
                bookingId, CancellationStatus.PENDING)) {
            throw new AppException(ErrorCode.BOOKING_CANCEL_PENDING);
        }

        long daysBeforeTour = ChronoUnit.DAYS.between(
                LocalDate.now(),
                booking.getSchedule().getDepartureDate());

        int refundPercent = calculateRefundPercent((int) daysBeforeTour);
        BigDecimal expectedRefund = booking.getPaidAmount()
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100));

        CancellationRequest cancellation = CancellationRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .reason(request.getReason())
                .daysBeforeTour((int) daysBeforeTour)
                .refundPercent(refundPercent)
                .expectedRefund(expectedRefund)
                .status(CancellationStatus.PENDING)
                .build();

        cancellationRepository.save(cancellation);

        log.info("Yêu cầu hủy booking: {} userId={}", bookingId, userId);

        CancellationResponse response = bookingMapper.toCancellationResponse(cancellation);
        response.setBookingCode(booking.getBookingCode());
        return response;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADMIN
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll().stream().map(bookingMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<BookingResponse> getByStatus(BookingStatus status) {
        return bookingRepository.findAllByStatusOrderByCreatedAtDesc(status)
                .stream().map(bookingMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<CancellationResponse> getAllCancellations() {
        return cancellationRepository
                .findAllByOrderByRequestedAtDesc()   // hoặc findAll() sort theo requestedAt
                .stream()
                .map(c -> {
                    CancellationResponse r = bookingMapper.toCancellationResponse(c);
                    bookingRepository.findById(c.getBookingId())
                            .ifPresent(b -> r.setBookingCode(b.getBookingCode()));
                    return r;
                })
                .toList();
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<BookingResponse> getByScheduleId(String scheduleId) {
        return bookingRepository.findAllBySchedule_ScheduleIdOrderByCreatedAtDesc(scheduleId)
                .stream().map(bookingMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<BookingResponse> getByUserId(String userId) {
        return bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(bookingMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public BookingResponse getById(String bookingId) {
        return bookingMapper.toResponse(findBookingById(bookingId));
    }

    /**
     * Cập nhật trạng thái booking.
     * <p>
     * Quy tắc bổ sung:
     * - Chỉ được chuyển sang COMPLETED khi paymentStatus = FULLY_PAID.
     * Analytics chỉ tính chi phí/doanh thu cho booking COMPLETED + FULLY_PAID.
     * - Khi COMPLETED: gửi thông báo nhắc đánh giá.
     * - Không cho phép thay đổi nếu đã CANCELLED hoặc COMPLETED.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookingResponse updateStatus(String bookingId, BookingStatus newStatus) {
        Booking booking = findBookingById(bookingId);
        BookingStatus old = booking.getStatus();

        // ── Guard: không đổi trạng thái của booking đã kết thúc ─────────────
        if (old == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANCELLED,
                    "Booking đã bị hủy, không thể thay đổi trạng thái.");
        }
        if (old == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_COMPLETED,
                    "Booking đã hoàn thành, không thể thay đổi trạng thái.");
        }

        // ── Guard: chỉ COMPLETED khi đã FULLY_PAID ───────────────────────────
        // Điều kiện này đảm bảo analytics luôn nhất quán:
        // chi phí & doanh thu chỉ được tính khi khách đã trả đủ tiền.
        if (newStatus == BookingStatus.COMPLETED
                && booking.getPaymentStatus() != BookingPaymentStatus.FULLY_PAID) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Không thể hoàn thành booking khi khách chưa thanh toán đủ. "
                            + "Trạng thái thanh toán hiện tại: "
                            + booking.getPaymentStatus().name());
        }

        booking.setStatus(newStatus);

        if (newStatus == BookingStatus.CONFIRMED && booking.getConfirmedAt() == null) {
            booking.setConfirmedAt(LocalDateTime.now());
        }

        bookingRepository.save(booking);

        // ── Thông báo nhắc đánh giá khi hoàn thành ───────────────────────────
        if (newStatus == BookingStatus.COMPLETED && old != BookingStatus.COMPLETED) {
            String tourTitle = booking.getSchedule().getTour().getTitle();
            String tourId = booking.getSchedule().getTour().getTourId();
            notificationService.sendIfNotExists(
                    booking.getUserId(),
                    NotificationType.REVIEW_REMINDER,
                    "Hãy đánh giá chuyến đi của bạn! ⭐",
                    "Chuyến tour \"" + tourTitle + "\" đã hoàn thành. "
                            + "Chia sẻ trải nghiệm của bạn để giúp những du khách khác nhé!",
                    tourId,
                    NotificationRelatedType.TOUR
            );
        }

        log.info("Cập nhật status booking {} : {} → {}", bookingId, old, newStatus);
        return bookingMapper.toResponse(booking);
    }

    /**
     * Admin hủy booking thủ công (không qua luồng CancellationRequest).
     * <p>
     * Bổ sung so với bản cũ:
     * - Set refundAmount = paidAmount × refundPercent (theo chính sách ngày hủy).
     * - Trả lại chỗ ngồi trên schedule.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookingResponse cancelByAdmin(String bookingId, String reason) {
        Booking booking = findBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANCELLED,
                    "Booking đã bị hủy trước đó.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_COMPLETED,
                    "Không thể hủy booking đã hoàn thành.");
        }

        // ── Tính hoàn tiền theo chính sách ngày hủy ──────────────────────────
        long daysBeforeTour = ChronoUnit.DAYS.between(
                LocalDate.now(),
                booking.getSchedule().getDepartureDate());
        int refundPercent = calculateRefundPercent((int) daysBeforeTour);
        BigDecimal refundAmount = booking.getPaidAmount()
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setRefundAmount(refundAmount);          // ← cập nhật refund
        bookingRepository.save(booking);

        // ── Trả lại ghế ──────────────────────────────────────────────────────
        TourSchedule schedule = booking.getSchedule();
        int totalPeople = booking.getNumAdults() + booking.getNumChildren();
        schedule.setBookedSeats(Math.max(0, schedule.getBookedSeats() - totalPeople));
        schedule.recalculate();
        scheduleRepository.save(schedule);

        // ── Thông báo khách ───────────────────────────────────────────────────
        String refundNote = refundPercent > 0
                ? " Hoàn tiền dự kiến: " + formatAmount(refundAmount) + " VNĐ ("
                + refundPercent + "%)."
                : " Không hoàn tiền theo chính sách hủy.";

        notificationService.send(
                booking.getUserId(),
                NotificationType.BOOKING_CANCELLED,
                "Đơn đặt tour đã bị hủy",
                "Đơn " + booking.getBookingCode() + " đã bị hủy. Lý do: "
                        + reason + "." + refundNote,
                bookingId,
                NotificationRelatedType.BOOKING
        );

        log.info("Admin hủy booking: {} refundPercent={}% refundAmount={}",
                bookingId, refundPercent, refundAmount);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<CancellationResponse> getPendingCancellations() {
        return cancellationRepository
                .findAllByStatusOrderByRequestedAtDesc(CancellationStatus.PENDING)
                .stream()
                .map(c -> {
                    CancellationResponse r = bookingMapper.toCancellationResponse(c);
                    bookingRepository.findById(c.getBookingId())
                            .ifPresent(b -> r.setBookingCode(b.getBookingCode()));
                    return r;
                })
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CancellationResponse reviewCancellation(String requestId, boolean approved,
                                                   CancellationReviewRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();

        CancellationRequest cancellation = cancellationRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CANCELLATION_NOT_FOUND));

        if (cancellation.getStatus() != CancellationStatus.PENDING) {
            throw new AppException(ErrorCode.CANCELLATION_ALREADY_REVIEWED);
        }

        cancellation.setStatus(approved ? CancellationStatus.APPROVED : CancellationStatus.REJECTED);
        cancellation.setReviewedBy(adminId);
        cancellation.setReviewedAt(LocalDateTime.now());
        cancellation.setAdminNote(request.getAdminNote());
        cancellationRepository.save(cancellation);

        if (approved) {
            Booking booking = findBookingById(cancellation.getBookingId());

            // ── Set refundAmount trên booking để analytics ghi nhận đúng ─────
            booking.setRefundAmount(cancellation.getExpectedRefund());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            booking.setCancellationReason(cancellation.getReason());
            bookingRepository.save(booking);

            TourSchedule schedule = booking.getSchedule();
            int totalPeople = booking.getNumAdults() + booking.getNumChildren();
            schedule.setBookedSeats(Math.max(0, schedule.getBookedSeats() - totalPeople));
            schedule.recalculate();
            scheduleRepository.save(schedule);

            notificationService.send(
                    cancellation.getUserId(),
                    NotificationType.CANCELLATION_APPROVED,
                    "Yêu cầu hủy đã được chấp thuận",
                    "Yêu cầu hủy đơn " + booking.getBookingCode()
                            + " đã được duyệt. Hoàn tiền dự kiến: "
                            + formatAmount(cancellation.getExpectedRefund()) + " VNĐ.",
                    cancellation.getRequestId(),
                    NotificationRelatedType.BOOKING
            );
        } else {
            Booking booking = findBookingById(cancellation.getBookingId());
            notificationService.send(
                    cancellation.getUserId(),
                    NotificationType.CANCELLATION_REJECTED,
                    "Yêu cầu hủy bị từ chối",
                    "Yêu cầu hủy đơn " + booking.getBookingCode()
                            + " đã bị từ chối. Lý do: "
                            + (request.getAdminNote() != null ? request.getAdminNote() : "Không có"),
                    cancellation.getRequestId(),
                    NotificationRelatedType.BOOKING
            );
        }

        log.info("Admin {} yêu cầu hủy: requestId={}",
                approved ? "duyệt" : "từ chối", requestId);

        CancellationResponse response = bookingMapper.toCancellationResponse(cancellation);
        bookingRepository.findById(cancellation.getBookingId())
                .ifPresent(b -> response.setBookingCode(b.getBookingCode()));
        return response;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    /**
     * Sinh mã booking không trùng lặp.
     * Format: BK + yyyyMMdd + 4 chữ số tăng dần (reset mỗi vòng 10000).
     * Ví dụ: BK202605230042
     */
    private String generateBookingCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = CODE_SEQ.incrementAndGet() % 10_000;
        return "BK" + date + String.format("%04d", seq);
    }

    /**
     * Chính sách hoàn tiền theo số ngày trước khởi hành.
     * > 15 ngày : hoàn 70%
     * 7–15 ngày : hoàn 50%
     * 3–6 ngày  : hoàn 30%
     * < 3 ngày  : không hoàn
     */
    private int calculateRefundPercent(int daysBeforeTour) {
        if (daysBeforeTour > 15) return 70;
        if (daysBeforeTour >= 7) return 50;
        if (daysBeforeTour >= 3) return 30;
        return 0;
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }
}