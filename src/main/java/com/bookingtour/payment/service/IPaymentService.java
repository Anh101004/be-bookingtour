package com.bookingtour.payment.service;

import com.bookingtour.payment.dto.request.PaymentConfirmRequest;
import com.bookingtour.payment.dto.request.PaymentCreateRequest;
import com.bookingtour.payment.dto.request.RefundRequest;
import com.bookingtour.payment.dto.response.PaymentResponse;
import com.bookingtour.payment.dto.response.PaymentSummaryResponse;
import com.bookingtour.payment.dto.response.RefundResponse;

import java.util.List;

public interface IPaymentService {

    // Customer + Admin
    PaymentSummaryResponse getSummaryByBookingId(String bookingId);

    List<PaymentResponse> getByBookingId(String bookingId);

    PaymentResponse getById(String paymentId);

    // Customer: tạo yêu cầu thanh toán
    PaymentResponse createPayment(PaymentCreateRequest request);

    // Admin: xác nhận đã nhận tiền

    List<RefundResponse> getAllRefunds();

    PaymentResponse confirmPayment(String paymentId, PaymentConfirmRequest request);

    // Admin: hủy thanh toán pending
    PaymentResponse cancelPayment(String paymentId);

    // Admin
    List<PaymentResponse> getAll();

    List<PaymentResponse> getPending();

    // Hoàn tiền
    RefundResponse createRefund(RefundRequest request);

    RefundResponse processRefund(String refundId, boolean approved, String note);

    List<RefundResponse> getRefundsByBookingId(String bookingId);

    List<RefundResponse> getPendingRefunds();
}