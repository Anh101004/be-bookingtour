package com.bookingtour.booking.mapper;

import com.bookingtour.booking.dto.response.BookingResponse;
import com.bookingtour.booking.dto.response.CancellationResponse;
import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.entity.CancellationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookingMapper {

    @Mapping(target = "scheduleId",    source = "schedule.scheduleId")
    @Mapping(target = "tourId",        source = "schedule.tour.tourId")
    @Mapping(target = "tourTitle",     source = "schedule.tour.title")
    @Mapping(target = "departureDate", source = "schedule.departureDate")
    @Mapping(target = "returnDate",    source = "schedule.returnDate")
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "bookingCode", ignore = true) // set thủ công trong service
    CancellationResponse toCancellationResponse(CancellationRequest request);
}