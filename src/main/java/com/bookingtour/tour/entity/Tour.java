package com.bookingtour.tour.entity;

import com.bookingtour.tourtype.entity.TourType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @Column(name = "tour_id", length = 36)
    private String tourId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private TourType tourType;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "slug", length = 255, nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    @Builder.Default
    private Integer durationDays = 1;

    @Column(name = "duration_nights", nullable = false)
    @Builder.Default
    private Integer durationNights = 0;

    @Column(name = "departure_location", length = 255)
    private String departureLocation;

    @Column(name = "destination", length = 255)
    private String destination;

    @Column(name = "price_adult", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal priceAdult = BigDecimal.ZERO;

    @Column(name = "price_child", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal priceChild = BigDecimal.ZERO;

    @Column(name = "vehicle", length = 255)
    private String vehicle;

    @Column(name = "hotel_standard", length = 100)
    private String hotelStandard;

    @Column(name = "included_services", columnDefinition = "TEXT")
    private String includedServices;

    @Column(name = "excluded_services", columnDefinition = "TEXT")
    private String excludedServices;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @Column(name = "gallery_images", columnDefinition = "TEXT")
    private String galleryImages;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<TourItinerary> itineraries = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourTypeMapping> typeMappings = new ArrayList<>();



    @PrePersist
    public void prePersist() {
        if (this.tourId == null || this.tourId.isBlank()) {
            this.tourId = java.util.UUID.randomUUID().toString();
        }
    }
}