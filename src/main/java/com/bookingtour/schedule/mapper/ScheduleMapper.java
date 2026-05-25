package com.bookingtour.schedule.mapper;

import com.bookingtour.schedule.dto.response.ScheduleHotelResponse;
import com.bookingtour.schedule.dto.response.ScheduleResponse;
import com.bookingtour.schedule.dto.response.ScheduleResponse.ItineraryDayResponse;
import com.bookingtour.schedule.dto.response.ScheduleVehicleResponse;
import com.bookingtour.schedule.entity.ScheduleHotel;
import com.bookingtour.schedule.entity.ScheduleVehicle;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.tour.entity.TourItinerary;
import com.bookingtour.vehicle.enums.VehicleType;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    // ── TourSchedule → ScheduleResponse ─────────────────────────
    @Mappings({
            @Mapping(target = "statusLabel",
                    expression = "java(statusLabel(s.getStatus()))"),

            // tour fields
            @Mapping(target = "tourId",            source = "tour.tourId"),
            @Mapping(target = "tourTitle",         source = "tour.title"),
            @Mapping(target = "destination",       source = "tour.destination"),
            @Mapping(target = "durationDays",      source = "tour.durationDays"),
            @Mapping(target = "durationNights",    source = "tour.durationNights"),
            @Mapping(target = "priceAdult",        source = "tour.priceAdult"),
            @Mapping(target = "priceChild",        source = "tour.priceChild"),
            @Mapping(target = "tourFeaturedImage", source = "tour.featuredImage"),

            // lịch trình theo ngày — lấy từ tour.itineraries
            @Mapping(target = "itineraries",
                    expression = "java(toItineraryResponses(s))"),

            // guide fields
            @Mapping(target = "guideId",       source = "guide.guideId"),
            @Mapping(target = "guideName",     source = "guide.fullName"),
            @Mapping(target = "guidePhone",    source = "guide.phone"),
            @Mapping(target = "guideLanguages",source = "guide.languages"),
            @Mapping(target = "guideAvatarUrl",source = "guide.avatarUrl"),

            // vehicle list
            @Mapping(target = "vehicles",
                    expression = "java(toVehicleResponses(s.getVehicles()))"),
            @Mapping(target = "totalTransportCostPerPerson",
                    expression = "java(sumTransport(s))"),

            // hotel list
            @Mapping(target = "hotels",
                    expression = "java(toHotelResponses(s.getHotels()))"),
            @Mapping(target = "totalHotelCost",
                    expression = "java(sumHotel(s))"),
            @Mapping(target = "pendingConfirmCount",
                    expression = "java(pendingCount(s))")
    })
    ScheduleResponse toResponse(TourSchedule s);

    // ── TourItinerary → ItineraryDayResponse ─────────────────────
    @Mappings({
            @Mapping(target = "itineraryId", source = "itineraryId"),
            @Mapping(target = "dayNumber",   source = "dayNumber"),
            @Mapping(target = "title",       source = "title"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "activities",  source = "activities"),
            @Mapping(target = "meals",       source = "meals"),
            @Mapping(target = "accommodation",source = "accommodation"),
            @Mapping(target = "imageUrl",    source = "imageUrl")
    })
    ItineraryDayResponse toItineraryDayResponse(TourItinerary it);

    // ── ScheduleVehicle → ScheduleVehicleResponse ────────────────
    @Mappings({
            @Mapping(target = "vehicleId",        source = "vehicle.vehicleId"),
            @Mapping(target = "vehicleName",      source = "vehicle.name"),
            @Mapping(target = "vehicleType",      source = "vehicle.type"),
            @Mapping(target = "vehicleTypeLabel",
                    expression = "java(typeLabel(sv.getVehicle().getType()))"),
            @Mapping(target = "licensePlate",     source = "vehicle.licensePlate"),
            @Mapping(target = "capacity",         source = "vehicle.capacity"),
            @Mapping(target = "features",         source = "vehicle.features"),
            @Mapping(target = "vehicleImageUrl",  source = "vehicle.imageUrl"),
            @Mapping(target = "estimatedDuration",
                    expression = "java(calcDuration(sv))")
    })
    ScheduleVehicleResponse toVehicleResponse(ScheduleVehicle sv);

    List<ScheduleVehicleResponse> toVehicleResponses(List<ScheduleVehicle> list);

    // ── ScheduleHotel → ScheduleHotelResponse ───────────────────
    @Mappings({
            @Mapping(target = "hotelId",            source = "hotel.hotelId"),
            @Mapping(target = "hotelName",          source = "hotel.name"),
            @Mapping(target = "starRating",         source = "hotel.starRating"),
            @Mapping(target = "hotelCity",          source = "hotel.city"),
            @Mapping(target = "hotelPhone",         source = "hotel.phone"),
            @Mapping(target = "hotelFeaturedImage", source = "hotel.featuredImage"),
            @Mapping(target = "checkInTime",        source = "hotel.checkInTime"),
            @Mapping(target = "checkOutTime",       source = "hotel.checkOutTime"),
            @Mapping(target = "roomId",             source = "room.roomId"),
            @Mapping(target = "roomType",           source = "room.roomType"),
            @Mapping(target = "bedType",            source = "room.bedType"),
            @Mapping(target = "roomCapacity",       source = "room.capacity"),
            @Mapping(target = "pricePerNight",      source = "room.pricePerNight"),
            @Mapping(target = "roomFeatures",       source = "room.features"),
            @Mapping(target = "roomImageUrl",       source = "room.imageUrl"),
            @Mapping(target = "numNights",
                    expression = "java(calcNights(sh))")
    })
    ScheduleHotelResponse toHotelResponse(ScheduleHotel sh);

    List<ScheduleHotelResponse> toHotelResponses(List<ScheduleHotel> list);

    // ════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════

    default List<ItineraryDayResponse> toItineraryResponses(TourSchedule s) {
        if (s.getTour() == null || s.getTour().getItineraries() == null)
            return Collections.emptyList();
        return s.getTour().getItineraries().stream()
                .sorted(Comparator.comparing(TourItinerary::getDayNumber))
                .map(this::toItineraryDayResponse)
                .toList();
    }

    default String statusLabel(ScheduleStatus status) {
        return status != null ? status.getLabel() : null;
    }

    default String typeLabel(String type) {
        try { return VehicleType.valueOf(type).getLabel(); }
        catch (Exception e) { return type; }
    }

    default String calcDuration(ScheduleVehicle sv) {
        if (sv.getDepartureTime() == null || sv.getArrivalTime() == null) return null;
        long minutes = java.time.Duration.between(
                sv.getDepartureTime(), sv.getArrivalTime()).toMinutes();
        if (minutes <= 0) return null;
        long h = minutes / 60, m = minutes % 60;
        if (h > 0 && m > 0) return h + " giờ " + m + " phút";
        if (h > 0)           return h + " giờ";
        return m + " phút";
    }

    default Integer calcNights(ScheduleHotel sh) {
        if (sh.getCheckInDate() == null || sh.getCheckOutDate() == null) return null;
        return (int) ChronoUnit.DAYS.between(sh.getCheckInDate(), sh.getCheckOutDate());
    }

    default BigDecimal sumTransport(TourSchedule s) {
        if (s.getVehicles() == null) return BigDecimal.ZERO;
        return s.getVehicles().stream()
                .map(ScheduleVehicle::getPricePerPerson)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumHotel(TourSchedule s) {
        if (s.getHotels() == null) return BigDecimal.ZERO;
        return s.getHotels().stream()
                .filter(sh -> sh.getTotalPrice() != null)
                .map(ScheduleHotel::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default long pendingCount(TourSchedule s) {
        if (s.getHotels() == null) return 0;
        return s.getHotels().stream()
                .filter(sh -> !Boolean.TRUE.equals(sh.getIsConfirmed()))
                .count();
    }
}