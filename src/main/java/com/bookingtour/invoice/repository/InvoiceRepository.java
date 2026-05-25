package com.bookingtour.invoice.repository;

import com.bookingtour.invoice.entity.Invoice;
import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    List<Invoice> findAllByBookingIdOrderByIssuedAtDesc(String bookingId);

    Optional<Invoice> findByBookingIdAndInvoiceType(String bookingId, InvoiceType type);

    boolean existsByBookingIdAndInvoiceType(String bookingId, InvoiceType type);

    List<Invoice> findAllByStatusOrderByIssuedAtDesc(InvoiceStatus status);

    // Lấy số HĐ mới nhất để generate số tiếp theo
    Optional<Invoice> findTopByOrderByInvoiceNumberDesc();
}