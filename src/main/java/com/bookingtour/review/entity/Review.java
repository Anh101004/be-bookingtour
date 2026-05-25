package com.bookingtour.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "review_id", length = 36)
    private String reviewId;

    // Không dùng @ManyToOne để tránh circular dependency với booking
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "tour_id", length = 36, nullable = false)
    private String tourId;

    @Column(name = "booking_id", length = 36, nullable = false, unique = true)
    private String bookingId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "guide_rating")
    private Integer guideRating; // 1-5

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // JSON array URL ảnh

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    // Admin ẩn nếu vi phạm
    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    @Column(name = "hidden_reason", columnDefinition = "TEXT")
    private String hiddenReason;

    // Admin phản hồi
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "replied_by", length = 36)
    private String repliedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.reviewId == null || this.reviewId.isBlank()) {
            this.reviewId = java.util.UUID.randomUUID().toString();
        }
    }
}