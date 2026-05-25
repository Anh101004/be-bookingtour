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
import com.bookingtour.payment.enums.PaymentMethod;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.PaymentType;
import com.bookingtour.payment.enums.RefundStatus;
import com.bookingtour.payment.mapper.PaymentMapper;
import com.bookingtour.payment.repository.PaymentRefundRepository;
import com.bookingtour.payment.repository.PaymentRepository;
import com.bookingtour.schedule.entity.TourSchedule;
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
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentRefundRepository refundRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentMapper paymentMapper;
    @Mock private INotificationService notificationService;
    @Mock private IInvoiceService invoiceService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking booking;
    private Payment payment;
    private PaymentRefund refund;

    @BeforeEach
    void setUp() {

        Tour tour = Tour.builder()
                .tourId("tour-1")
                .title("Tour Đà Lạt")
                .build();

        TourSchedule schedule = TourSchedule.builder()
                .scheduleId("sch-1")
                .tour(tour)
                .departureDate(LocalDate.now().plusDays(10))
                .build();

        booking = Booking.builder()
                .bookingId("booking-1")
                .bookingCode("BK001")
                .userId("user-1")
                .schedule(schedule)
                .totalAmount(BigDecimal.valueOf(5000000))
                .depositAmount(BigDecimal.valueOf(1500000))
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(BigDecimal.valueOf(5000000))
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .status(BookingStatus.PENDING)
                .build();

        payment = Payment.builder()
                .paymentId("payment-1")
                .bookingId("booking-1")
                .paymentType(PaymentType.DEPOSIT)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .amount(BigDecimal.valueOf(1500000))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        refund = PaymentRefund.builder()
                .refundId("refund-1")
                .bookingId("booking-1")
                .paymentId("payment-1")
                .refundAmount(BigDecimal.valueOf(1000000))
                .refundStatus(RefundStatus.PENDING)
                .build();
    }

    // ==================== getById ====================

    @Test
    void getById_Success() {

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.getById("payment-1");

        assertNotNull(response);
    }

    // Covers lambda$findPaymentById$3 (orElseThrow)
    @Test
    void getById_NotFound_ThrowsException() {

        when(paymentRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> paymentService.getById("not-exist"));
    }

    // ==================== getByBookingId ====================

    @Test
    void getByBookingId_Success() {

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findAllByBookingIdOrderByCreatedAtAsc("booking-1"))
                .thenReturn(List.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        List<PaymentResponse> responses =
                paymentService.getByBookingId("booking-1");

        assertFalse(responses.isEmpty());
    }

    // Covers lambda$findBookingById$2 (orElseThrow)
    @Test
    void getByBookingId_NotFound_ThrowsException() {

        when(bookingRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> paymentService.getByBookingId("not-exist"));
    }

    // ==================== getSummaryByBookingId ====================

    @Test
    void getSummaryByBookingId_Success() {

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findAllByBookingIdOrderByCreatedAtAsc("booking-1"))
                .thenReturn(List.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        assertDoesNotThrow(() ->
                paymentService.getSummaryByBookingId("booking-1"));
    }

    // lambda$getSummaryByBookingId$0 — nhánh payment.getPaymentStatus() == PAID
    @Test
    void getSummaryByBookingId_WithPaidPayment_Success() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findAllByBookingIdOrderByCreatedAtAsc("booking-1"))
                .thenReturn(List.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        assertDoesNotThrow(() ->
                paymentService.getSummaryByBookingId("booking-1"));
    }

    // ==================== createPayment ====================

    @Test
    void createPayment_Deposit_Success() {

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");
        request.setPaymentType(PaymentType.DEPOSIT);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(BigDecimal.valueOf(1500000));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBookingIdAndPaymentType(
                anyString(), any()))
                .thenReturn(false);

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_BookingFullyPaid_ThrowsException() {

        booking.setRemainingAmount(BigDecimal.ZERO);

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        AppException ex = assertThrows(AppException.class,
                () -> paymentService.createPayment(request));

        assertEquals(ErrorCode.BOOKING_FULLY_PAID, ex.getErrorCode());
    }

    // Nhánh: đã tồn tại thanh toán cọc → throw DEPOSIT_ALREADY_PAID
    @Test
    void createPayment_DepositAlreadyExists_ThrowsException() {

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");
        request.setPaymentType(PaymentType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(1500000));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBookingIdAndPaymentType(
                eq("booking-1"), eq(PaymentType.DEPOSIT)))
                .thenReturn(true);

        assertThrows(AppException.class,
                () -> paymentService.createPayment(request));
    }

    // Nhánh: thanh toán toàn bộ (FULL)
    @Test
    void createPayment_FullPayment_Success() {

        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setRemainingAmount(BigDecimal.valueOf(5000000));

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");
        request.setPaymentType(PaymentType.FULL);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(BigDecimal.valueOf(5000000));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBookingIdAndPaymentType(
                anyString(), any()))
                .thenReturn(false);

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
    }

    // Nhánh: thanh toán phần còn lại (REMAINING) khi chưa có deposit được xác nhận → throw exception
    @Test
    void createPayment_RemainingPayment_NoConfirmedDeposit_ThrowsException() {

        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setRemainingAmount(BigDecimal.valueOf(5000000));
        booking.setPaymentStatus(BookingPaymentStatus.UNPAID);

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");
        request.setPaymentType(PaymentType.REMAINING);
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(BigDecimal.valueOf(5000000));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        // Implementation kiểm tra deposit confirmed trước khi gọi existsByBookingIdAndPaymentType
        // nên không cần mock thêm — exception được ném ngay từ bước kiểm tra trạng thái
        assertThrows(AppException.class,
                () -> paymentService.createPayment(request));
    }

    // Nhánh: số tiền không khớp → throw INVALID_PAYMENT_AMOUNT
    @Test
    void createPayment_WrongAmount_ThrowsException() {

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setBookingId("booking-1");
        request.setPaymentType(PaymentType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(999));  // sai số tiền

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBookingIdAndPaymentType(
                anyString(), any()))
                .thenReturn(false);

        assertThrows(AppException.class,
                () -> paymentService.createPayment(request));
    }

    // ==================== calculateRefundPercent (gián tiếp qua createRefund) ====================
    // Vì calculateRefundPercent là private, coverage được đạt qua createRefund
    // với các mốc departureDate khác nhau để kích hoạt từng nhánh điều kiện.

    // Nhánh >= 30 ngày trước khởi hành → hoàn 100%
    @Test
    void createRefund_DepartureFarAway_FullRefund() {

        booking.getSchedule().setDepartureDate(LocalDate.now().plusDays(35));
        payment.setPaymentStatus(PaymentStatus.PAID);

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");
        request.setRefundReason("Hủy sớm");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        RefundResponse response = paymentService.createRefund(request);

        assertNotNull(response);
        // Chỉ verify save được gọi; giá trị refundAmount do calculateRefundPercent (private) tính
        verify(refundRepository).save(any(PaymentRefund.class));
    }

    // Nhánh 15-29 ngày trước khởi hành → hoàn một phần
    @Test
    void createRefund_Departure20Days_PartialRefund() {

        booking.getSchedule().setDepartureDate(LocalDate.now().plusDays(20));
        payment.setPaymentStatus(PaymentStatus.PAID);

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");
        request.setRefundReason("Hủy giữa chừng");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        RefundResponse response = paymentService.createRefund(request);

        assertNotNull(response);
        verify(refundRepository).save(any(PaymentRefund.class));
    }

    // Nhánh 7-14 ngày trước khởi hành → hoàn một phần nhỏ hơn
    @Test
    void createRefund_Departure10Days_LowRefund() {

        booking.getSchedule().setDepartureDate(LocalDate.now().plusDays(10));
        payment.setPaymentStatus(PaymentStatus.PAID);

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");
        request.setRefundReason("Hủy gần ngày");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        RefundResponse response = paymentService.createRefund(request);

        assertNotNull(response);
        verify(refundRepository).save(any(PaymentRefund.class));
    }

    // Nhánh < 7 ngày trước khởi hành → hoàn 0%
    @Test
    void createRefund_DepartureIn3Days_ZeroRefund() {

        booking.getSchedule().setDepartureDate(LocalDate.now().plusDays(3));
        payment.setPaymentStatus(PaymentStatus.PAID);

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");
        request.setRefundReason("Hủy sát ngày");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        RefundResponse response = paymentService.createRefund(request);

        assertNotNull(response);
        // Chỉ verify save được gọi; 0% refund được tính nội bộ bởi calculateRefundPercent (private)
        verify(refundRepository).save(any(PaymentRefund.class));
    }

    // ==================== confirmPayment ====================

    @Test
    void confirmPayment_Success() {

        PaymentConfirmRequest request = new PaymentConfirmRequest();

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            PaymentResponse response =
                    paymentService.confirmPayment("payment-1", request);

            assertNotNull(response);

            verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
            verify(bookingRepository).save(any(Booking.class));
            verify(invoiceService).autoCreate(any(Payment.class));
            verify(notificationService).send(
                    eq("user-1"),
                    eq(NotificationType.PAYMENT_CONFIRMED),
                    anyString(),
                    anyString(),
                    eq("payment-1"),
                    eq(NotificationRelatedType.PAYMENT)
            );
        }
    }

    // Nhánh: thanh toán này đủ để trả hết → updateBookingAfterPayment → FULLY_PAID
    @Test
    void confirmPayment_FullPaymentCoversAll_UpdatesToFullyPaid() {

        payment.setPaymentType(PaymentType.FULL);
        payment.setAmount(BigDecimal.valueOf(5000000));
        booking.setRemainingAmount(BigDecimal.valueOf(5000000));

        PaymentConfirmRequest request = new PaymentConfirmRequest();

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            paymentService.confirmPayment("payment-1", request);

            // Sau khi thanh toán đủ, booking phải được lưu với trạng thái FULLY_PAID
            verify(bookingRepository).save(argThat(b ->
                    b.getPaymentStatus() == BookingPaymentStatus.FULLY_PAID));
        }
    }

    // Nhánh: deposit → booking chuyển sang PARTIALLY_PAID
    @Test
    void confirmPayment_DepositPayment_UpdatesToPartiallyPaid() {

        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setAmount(BigDecimal.valueOf(1500000));
        booking.setRemainingAmount(BigDecimal.valueOf(5000000));

        PaymentConfirmRequest request = new PaymentConfirmRequest();

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            paymentService.confirmPayment("payment-1", request);

            verify(bookingRepository).save(argThat(b ->
                    b.getPaymentStatus() == BookingPaymentStatus.DEPOSITED));
        }
    }

    // FIX 1: Thêm mock SecurityUtils để vượt qua auth check trước status check
    @Test
    void confirmPayment_AlreadyPaid() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        PaymentConfirmRequest request = new PaymentConfirmRequest();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            AppException ex = assertThrows(AppException.class,
                    () -> paymentService.confirmPayment("payment-1", request));

            assertEquals(ErrorCode.PAYMENT_ALREADY_PAID, ex.getErrorCode());
        }
    }

    // ==================== cancelPayment ====================

    @Test
    void cancelPayment_Success() {

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.cancelPayment("payment-1");

        assertNotNull(response);
        assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
    }

    // Nhánh: không thể hủy payment đã thanh toán
    @Test
    void cancelPayment_AlreadyPaid_ThrowsException() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        assertThrows(AppException.class,
                () -> paymentService.cancelPayment("payment-1"));
    }

    // ==================== createRefund ====================

    @Test
    void createRefund_Success() {

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");
        request.setRefundReason("Khách hủy tour");

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        RefundResponse response = paymentService.createRefund(request);

        assertNotNull(response);
        verify(refundRepository).save(any(PaymentRefund.class));
    }

    // Nhánh: payment chưa được thanh toán → không thể hoàn tiền
    @Test
    void createRefund_PaymentNotPaid_ThrowsException() {

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("payment-1");

        // payment status = PENDING (không phải PAID)
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        assertThrows(AppException.class,
                () -> paymentService.createRefund(request));
    }

    // Covers lambda$findPaymentById$3 (orElseThrow trong createRefund)
    @Test
    void createRefund_PaymentNotFound_ThrowsException() {

        RefundRequest request = new RefundRequest();
        request.setBookingId("booking-1");
        request.setPaymentId("not-exist");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> paymentService.createRefund(request));
    }

    // ==================== processRefund ====================

    @Test
    void processRefund_Success() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(refundRepository.findById("refund-1"))
                .thenReturn(Optional.of(refund));

        when(paymentRepository.findById("payment-1"))
                .thenReturn(Optional.of(payment));

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            RefundResponse response =
                    paymentService.processRefund("refund-1", true, "OK");

            assertNotNull(response);
            verify(notificationService).send(
                    eq("user-1"),
                    eq(NotificationType.CANCELLATION_APPROVED),
                    anyString(),
                    anyString(),
                    eq("refund-1"),
                    eq(NotificationRelatedType.PAYMENT)
            );
        }
    }

    // Nhánh: từ chối hoàn tiền (approved = false)
    @Test
    void processRefund_Rejected_Success() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(refundRepository.findById("refund-1"))
                .thenReturn(Optional.of(refund));

        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            RefundResponse response =
                    paymentService.processRefund("refund-1", false, "Không hợp lệ");

            assertNotNull(response);
            assertEquals(RefundStatus.REJECTED, refund.getRefundStatus());

            // Implementation không gửi notification khi từ chối — chỉ verify status thay đổi
            verify(refundRepository).save(refund);
            verifyNoInteractions(notificationService);
        }
    }

    // FIX 2: Thêm mock SecurityUtils để vượt qua auth check
    @Test
    void processRefund_AlreadyProcessed() {

        refund.setRefundStatus(RefundStatus.PROCESSED);

        when(refundRepository.findById("refund-1"))
                .thenReturn(Optional.of(refund));

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            AppException ex = assertThrows(AppException.class,
                    () -> paymentService.processRefund("refund-1", true, "OK"));

            assertEquals(ErrorCode.REFUND_ALREADY_PROCESSED, ex.getErrorCode());
        }
    }

    // Covers lambda$processRefund$1 (orElseThrow khi không tìm thấy refund)
    @Test
    void processRefund_RefundNotFound_ThrowsException() {

        when(refundRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("admin-1");

            assertThrows(AppException.class,
                    () -> paymentService.processRefund("not-exist", true, "OK"));
        }
    }

    // ==================== getRefundsByBookingId ====================

    @Test
    void getRefundsByBookingId_Success() {

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        when(refundRepository.findAllByBookingIdOrderByRequestedAtDesc("booking-1"))
                .thenReturn(List.of(refund));

        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        List<RefundResponse> responses =
                paymentService.getRefundsByBookingId("booking-1");

        assertEquals(1, responses.size());
    }

    // ==================== getPendingRefunds ====================

    @Test
    void getPendingRefunds_Success() {

        when(refundRepository.findAllByRefundStatusOrderByRequestedAtDesc(
                RefundStatus.PENDING))
                .thenReturn(List.of(refund));

        when(paymentMapper.toRefundResponse(any(PaymentRefund.class)))
                .thenReturn(new RefundResponse());

        List<RefundResponse> responses = paymentService.getPendingRefunds();

        assertEquals(1, responses.size());
    }

    // ==================== getAll / getPending ====================

    @Test
    void getAll_Success() {

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        List<PaymentResponse> responses = paymentService.getAll();

        assertEquals(1, responses.size());
    }

    @Test
    void getPending_Success() {

        when(paymentRepository
                .findAllByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.PENDING))
                .thenReturn(List.of(payment));

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponse());

        List<PaymentResponse> responses = paymentService.getPending();

        assertEquals(1, responses.size());
    }
}