package com.bookingtour.hotel.dto.response;

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
public class HotelResponse {

    private String hotelId;
    private String name;
    private Integer starRating;
    private String address;
    private String city;
    private String province;
    private String phone;
    private String email;
    private String website;
    private String description;
    private String amenities;
    private String checkInTime;
    private String checkOutTime;
    private String featuredImage;
    private String galleryImages;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HotelRoomResponse> rooms;
}