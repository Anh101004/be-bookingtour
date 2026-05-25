package com.bookingtour.tour.mapper;

import com.bookingtour.tour.dto.response.FavoriteTourResponse;
import com.bookingtour.tour.entity.FavoriteTour;
import com.bookingtour.tour.entity.Tour;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi giữa FavoriteTour entity và DTO
 */
@Component
public class FavoriteTourMapper {

    /**
     * Chuyển FavoriteTour entity → FavoriteTourResponse
     * Yêu cầu entity đã fetch Tour (JOIN hoặc @EntityGraph)
     */
    public FavoriteTourResponse toResponse(FavoriteTour entity) {
        if (entity == null) return null;

        FavoriteTourResponse.FavoriteTourResponseBuilder builder = FavoriteTourResponse.builder()
                .favoriteId(entity.getFavoriteId())
                .addedAt(entity.getCreatedAt());

        Tour tour = entity.getTour();
        if (tour != null) {
            builder
                    .tourId(tour.getTourId())
                    .title(tour.getTitle())
                    .slug(tour.getSlug())
                    .destination(tour.getDestination())
                    .departureLocation(tour.getDepartureLocation())
                    .durationDays(tour.getDurationDays())
                    .durationNights(tour.getDurationNights())
                    .priceAdult(tour.getPriceAdult())
                    .priceChild(tour.getPriceChild())
                    .hotelStandard(tour.getHotelStandard())
                    .featuredImage(tour.getFeaturedImage())
                    .averageRating(tour.getAverageRating() != null ? tour.getAverageRating().doubleValue() : 0.0)
                    .ratingCount(tour.getRatingCount())
                    .isFeatured(tour.getIsFeatured());
        }

        return builder.build();
    }
}