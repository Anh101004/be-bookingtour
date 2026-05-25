package com.bookingtour.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @Column(name = "hotel_id", length = 36)
    private String hotelId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "star_rating", nullable = false)
    @Builder.Default
    private Integer starRating = 3;

    @Column(name = "address", length = 500, nullable = false)
    private String address;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;

    @Column(name = "check_in_time", nullable = false)
    @Builder.Default
    private LocalTime checkInTime = LocalTime.of(14, 0);

    @Column(name = "check_out_time", nullable = false)
    @Builder.Default
    private LocalTime checkOutTime = LocalTime.of(12, 0);

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @Column(name = "gallery_images", columnDefinition = "TEXT")
    private String galleryImages; // JSON array URL

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HotelRoom> rooms = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.hotelId == null || this.hotelId.isBlank()) {
            this.hotelId = java.util.UUID.randomUUID().toString();
        }
    }
}