package com.bookingtour.auth.repository;

import com.bookingtour.auth.entity.ActivityLog;
import com.bookingtour.auth.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<ActivityLog> findByUserIdAndActivityType(String userId, ActivityType activityType);
}