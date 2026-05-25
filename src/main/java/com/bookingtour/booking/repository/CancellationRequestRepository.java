package com.bookingtour.booking.repository;

import com.bookingtour.booking.entity.CancellationRequest;
import com.bookingtour.booking.enums.CancellationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, String> {

    List<CancellationRequest> findAllByUserIdOrderByRequestedAtDesc(String userId);

    List<CancellationRequest> findAllByStatusOrderByRequestedAtDesc(CancellationStatus status);

    Optional<CancellationRequest> findByBookingIdAndStatus(
            String bookingId, CancellationStatus status);

    boolean existsByBookingIdAndStatus(String bookingId, CancellationStatus status);
}