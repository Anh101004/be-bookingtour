package com.bookingtour.schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourItineraryResponse {
    private String itineraryId;
    private String tourId;
    private Integer dayNumber;
    private String title;
    private String description;
    private String activities;
    private String meals;
    private String accommodation;
    private String imageUrl;
}