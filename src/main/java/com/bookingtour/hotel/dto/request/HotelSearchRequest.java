package com.bookingtour.hotel.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelSearchRequest {

    private String city;

    private Integer starRating;
}