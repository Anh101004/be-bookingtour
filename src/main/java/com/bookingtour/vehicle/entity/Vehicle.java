package com.bookingtour.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @Column(name = "vehicle_id", length = 36)
    private String vehicleId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    /** BUS / VAN / BOAT / AIRPLANE / TRAIN */
    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "license_plate", length = 30)
    private String licensePlate;

    /** Số chỗ ngồi tối đa */
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "color", length = 50)
    private String color;

    /** Tiện nghi: Điều hòa, WiFi, toilet, USB... */
    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Giá thuê phương tiện mỗi ngày (VNĐ).
     * Dùng để tính chi phí vận chuyển khi lập lịch tour (trong Schedule module).
     */
    @Column(name = "price_per_day", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pricePerDay = BigDecimal.ZERO;

    /** AVAILABLE / IN_USE / MAINTENANCE / RETIRED */
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "AVAILABLE";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.vehicleId == null || this.vehicleId.isBlank()) {
            this.vehicleId = UUID.randomUUID().toString();
        }
    }
}