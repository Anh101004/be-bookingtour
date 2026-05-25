package com.bookingtour.tourtype.repository;

import com.bookingtour.tourtype.entity.TourType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourTypeRepository extends JpaRepository<TourType, String> {

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    Optional<TourType> findBySlug(String slug);

    List<TourType> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}