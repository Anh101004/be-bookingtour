package com.bookingtour.schedule.repository;

import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, String> {

    /** Tất cả lịch của 1 tour, sắp theo ngày khởi hành */
    List<TourSchedule> findAllByTour_TourIdOrderByDepartureDateAsc(String tourId);

    /** Lịch còn chỗ và chưa khởi hành (dành cho khách hàng) */
    @Query("""
            SELECT s FROM TourSchedule s
            WHERE s.tour.tourId = :tourId
              AND s.status = 'AVAILABLE'
              AND s.departureDate > :today
            ORDER BY s.departureDate ASC
            """)
    List<TourSchedule> findAvailableByTourId(
            @Param("tourId") String    tourId,
            @Param("today")  LocalDate today);

    /** Lọc theo status */
    List<TourSchedule> findAllByStatusOrderByDepartureDateAsc(ScheduleStatus status);

    /** Tất cả lịch của 1 HDV */
    List<TourSchedule> findAllByGuide_GuideIdOrderByDepartureDateAsc(String guideId);

    /**
     * Kiểm tra HDV đã bận trong khoảng ngày này chưa.
     * Dùng để tránh assign HDV bị trùng lịch.
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM TourSchedule s
            WHERE s.guide.guideId = :guideId
              AND s.status NOT IN ('CANCELLED', 'COMPLETED')
              AND s.departureDate < :returnDate
              AND s.returnDate    > :departureDate
            """)
    boolean isGuideBooked(
            @Param("guideId")       String    guideId,
            @Param("departureDate") LocalDate departureDate,
            @Param("returnDate")    LocalDate returnDate);

    /**
     * Kiểm tra khi update (bỏ qua chính schedule đang update)
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM TourSchedule s
            WHERE s.guide.guideId = :guideId
              AND s.scheduleId   != :excludeId
              AND s.status NOT IN ('CANCELLED', 'COMPLETED')
              AND s.departureDate < :returnDate
              AND s.returnDate    > :departureDate
            """)
    boolean isGuideBookedExclude(
            @Param("guideId")       String    guideId,
            @Param("departureDate") LocalDate departureDate,
            @Param("returnDate")    LocalDate returnDate,
            @Param("excludeId")     String    excludeId);
}