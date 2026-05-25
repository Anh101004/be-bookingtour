package com.bookingtour.tour.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Entity bảng favorite_tours - Tour yêu thích của khách hàng
 */
@Entity
@Table(
        name = "favorite_tours",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_favorite_user_tour",
                        columnNames = {"user_id", "tour_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteTour {

    @Id
    @UuidGenerator
    @Column(name = "favorite_id", length = 36, nullable = false, updatable = false)
    private String favoriteId;

    /** ID người dùng */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /** ID tour */
    @Column(name = "tour_id", length = 36, nullable = false)
    private String tourId;

    /** Thời điểm thêm vào yêu thích */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* ===== Quan hệ với Tour (chỉ đọc, không cascade) ===== */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", insertable = false, updatable = false)
    private Tour tour;
}