package com.bookingtour.tour.repository;

import com.bookingtour.tour.entity.TourItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourItineraryRepository extends JpaRepository<TourItinerary, String> {

    List<TourItinerary> findAllByTour_TourIdOrderByDayNumberAsc(String tourId);

    Optional<TourItinerary> findByTour_TourIdAndDayNumber(String tourId, Integer dayNumber);

    boolean existsByTour_TourIdAndDayNumber(String tourId, Integer dayNumber);

    boolean existsByTour_TourIdAndDayNumberAndItineraryIdNot(
            String tourId, Integer dayNumber, String excludeItineraryId);
}