package com.bookingtour.tour.dto.response;

import com.bookingtour.tourtype.dto.response.TourTypeResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourDetailResponse {

    private String tourId;
    private String title;
    private String slug;
    private String description;
    private Integer durationDays;
    private Integer durationNights;
    private String departureLocation;
    private String destination;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private String vehicle;
    private String hotelStandard;
    private String includedServices;
    private String excludedServices;
    private String featuredImage;
    private String galleryImages;
    private String videoUrl;
    private String highlights;
    private String notes;
    private Integer viewCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Boolean isFeatured;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private TourTypeResponse tourType;
    private List<TourTypeResponse> tourTypes;
    private List<TourItineraryResponse> itineraries;
}