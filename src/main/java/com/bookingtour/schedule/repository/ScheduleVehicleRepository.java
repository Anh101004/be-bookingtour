package com.bookingtour.schedule.repository;

import com.bookingtour.schedule.entity.ScheduleVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleVehicleRepository extends JpaRepository<ScheduleVehicle, Integer> {

    List<ScheduleVehicle> findAllBySchedule_ScheduleIdOrderByDepartureTimeAsc(String scheduleId);
}