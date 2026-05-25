package com.bookingtour.booking.repository;

import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.enums.BookingPaymentStatus;
import com.bookingtour.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<Booking> findAllBySchedule_ScheduleIdOrderByCreatedAtDesc(String scheduleId);

    List<Booking> findAllByStatusOrderByCreatedAtDesc(BookingStatus status);

    List<Booking> findAllByPaymentStatusOrderByCreatedAtDesc(BookingPaymentStatus paymentStatus);

    boolean existsByUserIdAndSchedule_ScheduleId(String userId, String scheduleId);
    boolean existsByUserIdAndSchedule_ScheduleIdAndStatusNot(
            String userId, String scheduleId, BookingStatus status);

    // Đếm số đặt confirmed/deposited theo schedule
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.schedule.scheduleId = :scheduleId
            AND b.status NOT IN ('CANCELLED')
            """)
    long countActiveByScheduleId(@Param("scheduleId") String scheduleId);

    // Tổng doanh thu theo khoảng ngày
    @Query("""
            SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b
            WHERE b.status IN ('CONFIRMED', 'COMPLETED')
            AND b.createdAt BETWEEN :from AND :to
            """)
    java.math.BigDecimal sumRevenueByDateRange(
            @Param("from") java.time.LocalDateTime from,
            @Param("to")   java.time.LocalDateTime to);

    // Lấy booking sắp hết hạn thanh toán (nhắc nhở)
    @Query("""
            SELECT b FROM Booking b
            WHERE b.paymentStatus = 'UNPAID'
            AND b.dueDate = :dueDate
            AND b.status NOT IN ('CANCELLED')
            """)
    List<Booking> findBookingsDueOn(@Param("dueDate") LocalDate dueDate);

    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.schedule s
        LEFT JOIN FETCH s.guide
        LEFT JOIN FETCH s.tour t
        LEFT JOIN FETCH t.itineraries
        WHERE b.bookingId = :id
    """)
    Optional<Booking> findByIdWithDetails(@Param("id") String id);
}