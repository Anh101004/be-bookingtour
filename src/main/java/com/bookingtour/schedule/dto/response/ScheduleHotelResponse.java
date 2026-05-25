package com.bookingtour.schedule.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class ScheduleHotelResponse {

    private Integer   id;
    private String    hotelId;
    private String    hotelName;
    private Integer   starRating;
    private String    hotelCity;
    private String    hotelPhone;
    private String    hotelFeaturedImage;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private String     roomId;
    private String     roomType;
    private String     bedType;
    private Integer    roomCapacity;
    private BigDecimal pricePerNight;
    private String     roomFeatures;
    private String     roomImageUrl;

    private LocalDate  checkInDate;
    private LocalDate  checkOutDate;
    private Integer    numNights;
    private Integer    numRooms;
    private BigDecimal totalPrice;
    private Boolean    isConfirmed;
    private String     notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}