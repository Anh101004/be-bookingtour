package com.bookingtour.payment.service.impl;

import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.invoice.service.IInvoiceService;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.service.INotificationService;
import com.bookingtour.payment.dto.request.PaymentConfirmRequest;
import com.bookingtour.payment.dto.request.PaymentCreateRequest;
import com.bookingtour.payment.dto.request.RefundRequest;
import com.bookingtour.payment.dto.response.PaymentResponse;
import com.bookingtour.payment.dto.response.PaymentSummaryResponse;
import com.bookingtour.payment.dto.response.RefundResponse;
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.payment.entity.PaymentRefund;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.PaymentType;
import com.bookingtour.payment.enums.RefundStatus;
import com.bookingtour.payment.mapper.PaymentMapper;
import com.bookingtour.payment.repository.PaymentRefundRepository;
import com.bookingtour.payment.repository.PaymentRepository;
import com.bookingtour.payment.service.IPaymentService;
import com.bookingtour.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository       paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final BookingRepository       bookingRepository;
    private final PaymentMapper           paymentMapper;
    private final INotificationService    notificationService;
    private final IInvoiceService         invoiceService;  // ✅ THÊM MỚI

    // ==================== QUERY ====================

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getSummaryByBookingId(String bookingId) {
        Booking booking = findBookingById(bookingId);
        List<Payment> payments = paymentRepository
                .findAllByBookingIdOrderByCreatedAtAsc(bookingId);

        LocalDateTime lastPayment = payments.stream()
                .filter(p -> p.getPaidAt() != null)
                .map(Payment::getPaidAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return PaymentSummaryResponse.builder()
                .bookingId(bookingId)
                .bookingCode(booking.getBookingCode())
                .totalAmount(booking.getTotalAmount())
                .depositAmount(booking.getDepositAmount())
                .paidAmount(booking.getPaidAmount())
                .remainingAmount(booking.getRemainingAmount())
                .paymentStatus(booking.getPaymentStatus())
                .dueDate(booking.getDueDate())
                .lastPaymentDate(lastPayment)
                .payments(payments.stream().map(paymentMapper::toResponse).toList())
                .build();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByBookingId(String bookingId) {
        findBookingById(bookingId);
        return paymentRepository.findAllByBookingIdOrderByCreatedAtAsc(bookingId)
                .stream().map(paymentMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PaymentResponse getById(String paymentId) {
        return paymentMapper.toResponse(findPaymentById(paymentId));
    }

    // ==================== CUSTOMER ====================

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Booking booking = findBookingById(request.getBookingId());

        // Kiểm tra booking đã thanh toán đủ chưa
        if (booking.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BOOKING_FULLY_PAID);
        }

        switch (request.getPaymentType()) {
            case DEPOSIT -> {
                if (paymentRepository.existsByBookingIdAndPaymentType(
                        request.getBookingId(), PaymentType.DEPOSIT)) {
                    throw new AppException(ErrorCode.PAYMENT_DEPOSIT_EXISTS);
                }
                if (request.getAmount().compareTo(booking.getDepositAmount()) != 0) {
                    throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID,
                            "Số tiền cọc phải là " + formatAmount(booking.getDepositAmount()) + " VNĐ");
                }
            }
            case FULL -> {
                if (paymentRepository.existsByBookingIdAndPaymentType(
                        request.getBookingId(), PaymentType.DEPOSIT)) {
                    throw new AppException(ErrorCode.PAYMENT_DEPOSIT_EXISTS,
                            "Đã có thanh toán cọc, vui lòng dùng loại REMAINING để trả phần còn lại");
                }
                request.setAmount(booking.getTotalAmount());
            }
            case REMAINING -> {
                if (!paymentRepository.existsByBookingIdAndPaymentTypeAndPaymentStatus(
                        request.getBookingId(), PaymentType.DEPOSIT, PaymentStatus.PAID)) {
                    throw new AppException(ErrorCode.VALIDATION_FAILED,
                            "Cần thanh toán cọc và được xác nhận trước khi trả phần còn lại");
                }
                if (request.getAmount().compareTo(booking.getRemainingAmount()) != 0) {
                    throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID,
                            "Số tiền còn lại phải là " + formatAmount(booking.getRemainingAmount()) + " VNĐ");
                }
            }
            case EXTRA -> {
                if (request.getAmount().compareTo(booking.getRemainingAmount()) > 0) {
                    throw new AppException(ErrorCode.PAYMENT_AMOUNT_INVALID,
                            "Số tiền không được vượt quá số tiền còn lại ("
                                    + formatAmount(booking.getRemainingAmount()) + " VNĐ)");
                }
            }
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .paymentType(request.getPaymentType())
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .dueDate(request.getDueDate())
                .transactionId(request.getTransactionId())
                .paymentNote(request.getPaymentNote())
                .build();

        paymentRepository.save(payment);
        log.info("Tạo thanh toán: bookingId={} amount={} type={}",
                request.getBookingId(), request.getAmount(), request.getPaymentType());
        return paymentMapper.toResponse(payment);
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PaymentResponse confirmPayment(String paymentId, PaymentConfirmRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        Payment payment = findPaymentById(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        // Cập nhật payment → PAID
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setConfirmedBy(adminId);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now());
        if (request.getTransactionId() != null) payment.setTransactionId(request.getTransactionId());
        if (request.getPaymentNote()   != null) payment.setPaymentNote(request.getPaymentNote());
        paymentRepository.save(payment);

        // Cập nhật paid_amount, remaining_amount, status booking
        updateBookingAfterPayment(payment);

        // ✅ TỰ ĐỘNG TẠO HÓA ĐƠN + GỬI EMAIL CHO KHÁCH
        // Lỗi PDF/mail không ảnh hưởng luồng thanh toán (đã try-catch bên trong)
        invoiceService.autoCreate(payment);

        // Gửi thông báo trong app
        Booking booking = findBookingById(payment.getBookingId());
        notificationService.send(
                booking.getUserId(),
                NotificationType.PAYMENT_CONFIRMED,
                "Xác nhận thanh toán thành công",
                "Thanh toán " + formatAmount(payment.getAmount())
                        + " VNĐ cho đơn " + booking.getBookingCode() + " đã được xác nhận.",
                payment.getPaymentId(),
                NotificationRelatedType.PAYMENT
        );

        log.info("Xác nhận thanh toán: paymentId={}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PaymentResponse cancelPayment(String paymentId) {
        Payment payment = findPaymentById(paymentId);
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        log.info("Hủy thanh toán: paymentId={}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPending() {
        return paymentRepository
                .findAllByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.PENDING)
                .stream().map(paymentMapper::toResponse).toList();
    }

    // ==================== HOÀN TIỀN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<RefundResponse> getAllRefunds() {
        return refundRepository
                .findAllByOrderByRequestedAtDesc()
                .stream()
                .map(paymentMapper::toRefundResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RefundResponse createRefund(RefundRequest request) {
        Booking booking = findBookingById(request.getBookingId());
        Payment payment = findPaymentById(request.getPaymentId());

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Chỉ hoàn tiền cho thanh toán đã xác nhận");
        }

        long daysBeforeTour = ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                booking.getSchedule().getDepartureDate());

        int refundPercent = calculateRefundPercent((int) daysBeforeTour);
        BigDecimal refundAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100));

        PaymentRefund refund = PaymentRefund.builder()
                .bookingId(request.getBookingId())
                .paymentId(request.getPaymentId())
                .refundAmount(refundAmount)
                .refundPercent(refundPercent)
                .daysBeforeTour((int) daysBeforeTour)
                .refundReason(request.getRefundReason())
                .refundStatus(RefundStatus.PENDING)
                .build();

        refundRepository.save(refund);
        log.info("Tạo yêu cầu hoàn tiền: bookingId={} amount={} percent={}%",
                request.getBookingId(), refundAmount, refundPercent);
        return paymentMapper.toRefundResponse(refund);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RefundResponse processRefund(String refundId, boolean approved, String note) {
        String adminId = SecurityUtils.getCurrentUserId();
        PaymentRefund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND));

        if (refund.getRefundStatus() != RefundStatus.PENDING) {
            throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED);
        }

        refund.setRefundStatus(approved ? RefundStatus.PROCESSED : RefundStatus.REJECTED);
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(adminId);
        refund.setNote(note);
        refundRepository.save(refund);

        if (approved) {
            // Cập nhật payment → REFUNDED
            Payment payment = findPaymentById(refund.getPaymentId());
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // Cập nhật booking
            Booking booking = findBookingById(refund.getBookingId());
            booking.setRefundAmount(refund.getRefundAmount());
            booking.setPaymentStatus(BookingPaymentStatus.REFUNDED);
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // Thông báo
            notificationService.send(
                    booking.getUserId(),
                    NotificationType.CANCELLATION_APPROVED,
                    "Hoàn tiền thành công",
                    "Yêu cầu hoàn " + formatAmount(refund.getRefundAmount())
                            + " VNĐ cho đơn " + booking.getBookingCode() + " đã được xử lý.",
                    refund.getRefundId(),
                    NotificationRelatedType.PAYMENT
            );
        }

        log.info("Xử lý hoàn tiền: refundId={} approved={}", refundId, approved);
        return paymentMapper.toRefundResponse(refund);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByBookingId(String bookingId) {
        findBookingById(bookingId);
        return refundRepository.findAllByBookingIdOrderByRequestedAtDesc(bookingId)
                .stream().map(paymentMapper::toRefundResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<RefundResponse> getPendingRefunds() {
        return refundRepository
                .findAllByRefundStatusOrderByRequestedAtDesc(RefundStatus.PENDING)
                .stream().map(paymentMapper::toRefundResponse).toList();
    }

    // ==================== PRIVATE ====================

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    private Payment findPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private void updateBookingAfterPayment(Payment payment) {
        Booking booking = findBookingById(payment.getBookingId());

        BigDecimal newPaid      = booking.getPaidAmount().add(payment.getAmount());
        BigDecimal newRemaining = booking.getTotalAmount().subtract(newPaid);

        booking.setPaidAmount(newPaid);
        booking.setRemainingAmount(newRemaining.max(BigDecimal.ZERO));

        if (newPaid.compareTo(booking.getTotalAmount()) >= 0) {
            booking.setPaymentStatus(BookingPaymentStatus.FULLY_PAID);
            booking.setStatus(BookingStatus.CONFIRMED);
        } else if (newPaid.compareTo(booking.getDepositAmount()) >= 0) {
            booking.setPaymentStatus(BookingPaymentStatus.DEPOSITED);
            booking.setStatus(BookingStatus.DEPOSITED);
        }

        bookingRepository.save(booking);
    }

    /**
     * Chính sách hoàn tiền:
     * > 15 ngày → 70%
     * 7–15 ngày → 50%
     * 3–7 ngày  → 30%
     * < 3 ngày  → 0%
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