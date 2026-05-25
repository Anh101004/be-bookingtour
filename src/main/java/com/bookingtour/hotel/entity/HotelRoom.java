package com.bookingtour.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRoom {

    @Id
    @Column(name = "room_id", length = 36)
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "room_type", length = 100, nullable = false)
    private String roomType; // Standard/Superior/Deluxe/Suite

    @Column(name = "bed_type", length = 100)
    private String bedType; // Single/Double/Twin/King

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 2;

    @Column(name = "total_rooms", nullable = false)
    @Builder.Default
    private Integer totalRooms = 1;

    @Column(name = "price_per_night", precision = 12, scale = 2, nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "area_sqm", precision = 6, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
        if (this.roomId == null || this.roomId.isBlank()) {
            this.roomId = java.util.UUID.randomUUID().toString();
        }
    }
}