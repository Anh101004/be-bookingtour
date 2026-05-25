package com.bookingtour.guide.repository;

import com.bookingtour.guide.entity.GuideSchedule;
import com.bookingtour.guide.enums.GuideScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GuideScheduleRepository extends JpaRepository<GuideSchedule, Integer> {

    List<GuideSchedule> findAllByGuideIdOrderByStartDateDesc(String guideId);

    List<GuideSchedule> findAllByGuideIdAndStatus(String guideId, GuideScheduleStatus status);

    /**
     * Kiểm tra HDV có bận (có lịch trùng) trong khoảng ngày không.
     * Overlap condition: startDate <= :endDate AND endDate >= :startDate
     */
    @Query("""
            SELECT COUNT(gs) > 0 FROM GuideSchedule gs
            WHERE gs.guideId = :guideId
              AND gs.startDate <= :endDate
              AND gs.endDate   >= :startDate
            """)
    boolean isGuideBusy(@Param("guideId") String guideId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}