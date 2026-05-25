package com.bookingtour.guide.repository;

import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.guide.enums.GuideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourGuideRepository extends JpaRepository<TourGuide, String> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<TourGuide> findAllByIsActiveTrueOrderByFullNameAsc();

    List<TourGuide> findAllByStatusAndIsActiveTrue(GuideStatus status);
}