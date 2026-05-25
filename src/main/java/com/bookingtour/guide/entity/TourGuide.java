package com.bookingtour.guide.entity;

import com.bookingtour.guide.enums.GuideStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_guides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourGuide {

    @Id
    @Column(name = "guide_id", length = 36)
    private String guideId;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "experience_years", nullable = false)
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "languages", length = 255)
    private String languages;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "specialties", length = 255)
    private String specialties;

    @Column(name = "average_rating", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_tours", nullable = false)
    @Builder.Default
    private Integer totalTours = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private GuideStatus status = GuideStatus.AVAILABLE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.guideId == null || this.guideId.isBlank()) {
            this.guideId = java.util.UUID.randomUUID().toString();
        }
    }
}