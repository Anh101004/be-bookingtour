package com.bookingtour.tour.mapper;

import com.bookingtour.tour.dto.request.TourItineraryRequest;
import com.bookingtour.tour.dto.response.TourDetailResponse;
import com.bookingtour.tour.dto.response.TourItineraryResponse;
import com.bookingtour.tour.dto.response.TourResponse;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.entity.TourItinerary;
import com.bookingtour.tourtype.mapper.TourTypeMapper;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = { TourTypeMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TourMapper {

    @Mapping(target = "tourType",   source = "tourType")
    @Mapping(target = "tourTypes",  expression = "java(tour.getTypeMappings().stream().map(m -> tourTypeMapper.toResponse(m.getTourType())).toList())")
    @Mapping(target = "itineraries", source = "itineraries")
    TourResponse toResponse(Tour tour, @Context TourTypeMapper tourTypeMapper);

    @Mapping(target = "tourType",   source = "tourType")
    @Mapping(target = "tourTypes",  expression = "java(tour.getTypeMappings().stream().map(m -> tourTypeMapper.toResponse(m.getTourType())).toList())")
    @Mapping(target = "itineraries", source = "itineraries")
    TourDetailResponse toDetailResponse(Tour tour, @Context TourTypeMapper tourTypeMapper);

    @Mapping(target = "tourId",    source = "tour.tourId")
    TourItineraryResponse toItineraryResponse(TourItinerary itinerary);

    @Mapping(target = "itineraryId", ignore = true)
    @Mapping(target = "tour",        ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    TourItinerary toItineraryEntity(TourItineraryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "itineraryId", ignore = true)
    @Mapping(target = "tour",        ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    void updateItineraryEntity(TourItineraryRequest request, @MappingTarget TourItinerary itinerary);
}