package com.bookingtour.auth.entity;

import com.bookingtour.auth.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @Column(name = "log_id", length = 36)
    private String logId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50, nullable = false)
    private ActivityType activityType;

    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.logId == null || this.logId.isBlank()) {
            this.logId = java.util.UUID.randomUUID().toString();
        }
    }
}