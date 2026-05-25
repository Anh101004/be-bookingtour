package com.bookingtour.notification.repository;

import com.bookingtour.notification.entity.Notification;
import com.bookingtour.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    long countByUserIdAndIsReadFalse(String userId);

    boolean existsByRelatedIdAndType(String relatedId, NotificationType type);

    // ── Mark as read ─────────────────────────────────────────────────────────

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
            WHERE n.notificationId = :id AND n.userId = :userId
            """)
    void markOneAsRead(@Param("id") String notificationId,
                       @Param("userId") String userId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
            WHERE n.userId = :userId AND n.isRead = false
            """)
    void markAllAsRead(@Param("userId") String userId);

    // ── Delete ───────────────────────────────────────────────────────────────

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.notificationId = :id AND n.userId = :userId")
    int deleteOneByIdAndUserId(@Param("id") String notificationId,
                               @Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    // ── Scheduler: tìm booking sắp khởi hành, chưa gửi nhắc ─────────────────

    /**
     * Lấy các booking CONFIRMED có lịch khởi hành = targetDate (ngày mai),
     * chưa tồn tại thông báo DEPARTURE_REMINDER cho bookingId đó.
     * Trả về: [userId, bookingId, customerName, scheduleId, departureDate, tourTitle]
     */
    @Query("""
            SELECT b.userId,
                   b.bookingId,
                   b.customerName,
                   s.scheduleId,
                   s.departureDate,
                   t.title
            FROM Booking b
            JOIN b.schedule s
            JOIN s.tour    t
            WHERE s.departureDate = :targetDate
              AND b.status        = 'CONFIRMED'
              AND NOT EXISTS (
                  SELECT 1 FROM Notification n
                  WHERE n.relatedId = b.bookingId
                    AND n.type      = com.bookingtour.notification.enums.NotificationType.DEPARTURE_REMINDER
              )
            """)
    List<Object[]> findBookingsDepartingOn(@Param("targetDate") LocalDate targetDate);
}