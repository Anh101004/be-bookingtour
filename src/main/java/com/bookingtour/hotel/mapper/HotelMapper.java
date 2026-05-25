package com.bookingtour.hotel.mapper;

import com.bookingtour.hotel.dto.request.HotelCreateRequest;
import com.bookingtour.hotel.dto.request.HotelRoomRequest;
import com.bookingtour.hotel.dto.request.HotelUpdateRequest;
import com.bookingtour.hotel.dto.response.HotelResponse;
import com.bookingtour.hotel.dto.response.HotelRoomResponse;
import com.bookingtour.hotel.entity.Hotel;
import com.bookingtour.hotel.entity.HotelRoom;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HotelMapper {

    @Mapping(target = "rooms", source = "rooms")
    @Mapping(target = "checkInTime",  expression = "java(hotel.getCheckInTime()  != null ? hotel.getCheckInTime().toString()  : null)")
    @Mapping(target = "checkOutTime", expression = "java(hotel.getCheckOutTime() != null ? hotel.getCheckOutTime().toString() : null)")
    HotelResponse toResponse(Hotel hotel);

    @Mapping(target = "hotelId",      ignore = true)
    @Mapping(target = "rooms",        ignore = true)
    @Mapping(target = "featuredImage",ignore = true)
    @Mapping(target = "galleryImages",ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "checkInTime",  expression = "java(request.getCheckInTime()  != null ? java.time.LocalTime.parse(request.getCheckInTime())  : java.time.LocalTime.of(14, 0))")
    @Mapping(target = "checkOutTime", expression = "java(request.getCheckOutTime() != null ? java.time.LocalTime.parse(request.getCheckOutTime()) : java.time.LocalTime.of(12, 0))")
    Hotel toEntity(HotelCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "hotelId",      ignore = true)
    @Mapping(target = "rooms",        ignore = true)
    @Mapping(target = "featuredImage",ignore = true)
    @Mapping(target = "galleryImages",ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "checkInTime",  expression = "java(request.getCheckInTime()  != null ? java.time.LocalTime.parse(request.getCheckInTime())  : hotel.getCheckInTime())")
    @Mapping(target = "checkOutTime", expression = "java(request.getCheckOutTime() != null ? java.time.LocalTime.parse(request.getCheckOutTime()) : hotel.getCheckOutTime())")
    void updateEntity(HotelUpdateRequest request, @MappingTarget Hotel hotel);

    @Mapping(target = "hotelId", source = "hotel.hotelId")
    HotelRoomResponse toRoomResponse(HotelRoom room);

    @Mapping(target = "roomId",    ignore = true)
    @Mapping(target = "hotel",     ignore = true)
    @Mapping(target = "isActive",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HotelRoom toRoomEntity(HotelRoomRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roomId",    ignore = true)
    @Mapping(target = "hotel",     ignore = true)
    @Mapping(target = "isActive",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateRoomEntity(HotelRoomRequest request, @MappingTarget HotelRoom room);
}