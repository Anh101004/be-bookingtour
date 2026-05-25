package com.bookingtour.schedule.entity;

import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.tour.entity.Tour;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tour_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSchedule {

    @Id
    @Column(name = "schedule_id", length = 36)
    private String scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private TourGuide guide;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    @Column(name = "booked_seats", nullable = false)
    @Builder.Default
    private Integer bookedSeats = 0;

    @Column(name = "available_seats", nullable = false)
    @Builder.Default
    private Integer availableSeats = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.AVAILABLE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relations ────────────────────────────────────────────────

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleVehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleHotel> hotels = new ArrayList<>();

    // ── Hooks ────────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null || this.scheduleId.isBlank()) {
            this.scheduleId = UUID.randomUUID().toString();
        }
        this.availableSeats = this.maxSeats - this.bookedSeats;
    }

    /** Tính lại availableSeats + tự động cập nhật status FULL/AVAILABLE */
    public void recalculate() {
        this.availableSeats = this.maxSeats - this.bookedSeats;
        if (this.status == ScheduleStatus.AVAILABLE && this.availableSeats <= 0) {
            this.status = ScheduleStatus.FULL;
        } else if (this.status == ScheduleStatus.FULL && this.availableSeats > 0) {
            this.status = ScheduleStatus.AVAILABLE;
        }
    }
}