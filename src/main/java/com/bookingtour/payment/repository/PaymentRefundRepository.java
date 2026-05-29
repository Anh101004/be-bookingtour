package com.bookingtour.payment.repository;

import com.bookingtour.payment.entity.PaymentRefund;
import com.bookingtour.payment.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, String> {

    List<PaymentRefund> findAllByBookingIdOrderByRequestedAtDesc(String bookingId);

    List<PaymentRefund> findAllByRefundStatusOrderByRequestedAtDesc(RefundStatus status);
    List<PaymentRefund> findAllByOrderByRequestedAtDesc();

}