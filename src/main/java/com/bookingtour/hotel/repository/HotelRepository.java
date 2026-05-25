package com.bookingtour.hotel.repository;

import com.bookingtour.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, String> {

    List<Hotel> findAllByIsActiveTrueOrderByNameAsc();

    List<Hotel> findAllByCityIgnoreCaseAndIsActiveTrue(String city);

    List<Hotel> findAllByStarRatingAndIsActiveTrue(Integer starRating);

    @Query("""
            SELECT h FROM Hotel h
            WHERE h.isActive = true
            AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
            AND (:starRating IS NULL OR h.starRating = :starRating)
            ORDER BY h.starRating DESC, h.name ASC
            """)
    List<Hotel> searchHotels(@Param("city") String city,
                             @Param("starRating") Integer starRating);
}