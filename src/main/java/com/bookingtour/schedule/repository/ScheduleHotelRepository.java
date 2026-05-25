package com.bookingtour.schedule.repository;

import com.bookingtour.schedule.entity.ScheduleHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleHotelRepository extends JpaRepository<ScheduleHotel, Integer> {

    List<ScheduleHotel> findAllBySchedule_ScheduleIdOrderByCheckInDateAsc(String scheduleId);
}