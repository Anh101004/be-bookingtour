package com.bookingtour.hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelRoomResponse {

    private String roomId;
    private String hotelId;
    private String roomType;
    private String bedType;
    private Integer capacity;
    private Integer totalRooms;
    private BigDecimal pricePerNight;
    private BigDecimal areaSqm;
    private String features;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}