package com.bookingtour.tour.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_itineraries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tour_id", "day_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourItinerary {

    @Id
    @Column(name = "itinerary_id", length = 36)
    private String itineraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "meals", length = 100)
    private String meals;

    @Column(name = "accommodation", length = 255)
    private String accommodation;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.itineraryId == null || this.itineraryId.isBlank()) {
            this.itineraryId = java.util.UUID.randomUUID().toString();
        }
    }
}