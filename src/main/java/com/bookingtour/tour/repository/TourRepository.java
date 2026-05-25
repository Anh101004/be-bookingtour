package com.bookingtour.tour.repository;

import com.bookingtour.tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {

    boolean existsBySlug(String slug);

    Optional<Tour> findBySlug(String slug);

    List<Tour> findAllByIsActiveTrueOrderByCreatedAtDesc();

    List<Tour> findAllByIsFeaturedTrueAndIsActiveTrue();

    @Query("""
            SELECT t FROM Tour t
            WHERE t.isActive = true
            AND (:destination IS NULL OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
            AND (:minPrice IS NULL OR t.priceAdult >= :minPrice)
            AND (:maxPrice IS NULL OR t.priceAdult <= :maxPrice)
            AND (:durationDays IS NULL OR t.durationDays = :durationDays)
            ORDER BY t.createdAt DESC
            """)
    List<Tour> searchTours(@Param("destination") String destination,
                           @Param("minPrice")    BigDecimal minPrice,
                           @Param("maxPrice")    BigDecimal maxPrice,
                           @Param("durationDays") Integer durationDays);

    @Modifying
    @Query("UPDATE Tour t SET t.viewCount = t.viewCount + 1 WHERE t.tourId = :tourId")
    void incrementViewCount(@Param("tourId") String tourId);

    boolean existsByTourIdAndIsActiveTrue(String tourId);

    Optional<Tour> findByTourIdAndIsActiveTrue(String tourId);
}