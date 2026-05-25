package com.bookingtour.tour.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TourSearchRequest {

    private String destination;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer durationDays;
}