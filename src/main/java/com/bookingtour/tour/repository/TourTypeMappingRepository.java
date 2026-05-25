package com.bookingtour.tour.repository;

import com.bookingtour.tour.entity.TourTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourTypeMappingRepository extends JpaRepository<TourTypeMapping, Integer> {

    List<TourTypeMapping> findAllByTour_TourId(String tourId);

    void deleteByTour_TourId(String tourId);

    boolean existsByTour_TourIdAndTourType_TypeId(String tourId, String typeId);
}