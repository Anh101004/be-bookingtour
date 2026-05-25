package com.bookingtour.payment.repository;

import com.bookingtour.payment.entity.Payment;
import com.bookingtour.payment.enums.PaymentStatus;
import com.bookingtour.payment.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findAllByBookingIdOrderByCreatedAtAsc(String bookingId);

    List<Payment> findAllByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    boolean existsByBookingIdAndPaymentTypeAndPaymentStatus(
            String bookingId, PaymentType paymentType, PaymentStatus paymentStatus);

    boolean existsByBookingIdAndPaymentType(String bookingId, PaymentType paymentType);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.bookingId = :bookingId
            AND p.paymentStatus = 'PAID'
            """)
    BigDecimal sumPaidByBookingId(@Param("bookingId") String bookingId);
}