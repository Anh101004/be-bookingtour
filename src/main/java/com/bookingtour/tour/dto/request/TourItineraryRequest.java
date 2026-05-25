package com.bookingtour.tour.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourItineraryRequest {

    @NotNull(message = "Số ngày không được để trống")
    @Min(value = 1, message = "Số ngày tối thiểu là 1")
    private Integer dayNumber;

    @NotBlank(message = "Tiêu đề ngày không được để trống")
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;

    private String description;

    private String activities;

    @Size(max = 100, message = "Thông tin bữa ăn không được quá 100 ký tự")
    private String meals;

    @Size(max = 255, message = "Nơi lưu trú không được quá 255 ký tự")
    private String accommodation;

    @Size(max = 500)
    private String imageUrl;
}