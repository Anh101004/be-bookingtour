package com.bookingtour.review.repository;

import com.bookingtour.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    // Public — chỉ lấy review không bị ẩn
    List<Review> findAllByTourIdAndIsHiddenFalseOrderByCreatedAtDesc(String tourId);

    // Admin — lấy tất cả kể cả bị ẩn
    List<Review> findAllByTourIdOrderByCreatedAtDesc(String tourId);

    List<Review> findAllByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByBookingId(String bookingId);

    Optional<Review> findByBookingId(String bookingId);

    Optional<Review> findByBookingIdAndUserId(String bookingId, String userId);

    @Query("""
            SELECT AVG(r.rating) FROM Review r
            WHERE r.tourId = :tourId AND r.isHidden = false
            """)
    Double getAverageRatingByTourId(@Param("tourId") String tourId);

    @Query("""
            SELECT COUNT(r) FROM Review r
            WHERE r.tourId = :tourId AND r.isHidden = false
            """)
    Long countVisibleByTourId(@Param("tourId") String tourId);
}