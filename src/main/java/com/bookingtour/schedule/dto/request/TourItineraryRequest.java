package com.bookingtour.schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TourItineraryRequest {

    @NotNull(message = "Số ngày không được để trống")
    @Min(value = 1, message = "Số ngày phải >= 1")
    private Integer dayNumber;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
    private String activities;

    /** VD: "Sáng, Trưa, Tối" */
    private String meals;

    private String accommodation;
    private String imageUrl;
}