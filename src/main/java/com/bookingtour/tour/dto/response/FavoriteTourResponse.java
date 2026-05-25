package com.bookingtour.tour.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response trả về thông tin tour yêu thích
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteTourResponse {

    // ===== Thông tin yêu thích =====
    private String favoriteId;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime addedAt;  // Ngày thêm vào yêu thích (created_at)

    // ===== Thông tin tour =====
    private String tourId;
    private String title;
    private String slug;
    private String destination;
    private String departureLocation;
    private Integer durationDays;
    private Integer durationNights;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private String hotelStandard;
    private String featuredImage;
    private Double averageRating;
    private Integer ratingCount;
    private Boolean isFeatured;
}