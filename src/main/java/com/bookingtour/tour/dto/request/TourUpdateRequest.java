package com.bookingtour.tour.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TourUpdateRequest {

    @NotBlank(message = "Tiêu đề tour không được để trống")
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;

    @NotBlank(message = "Slug không được để trống")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug chỉ chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    private String typeId;

    private List<String> typeIds;

    private String description;

    @Min(value = 1, message = "Số ngày tối thiểu là 1")
    private Integer durationDays;

    @Min(value = 0, message = "Số đêm không được âm")
    private Integer durationNights;

    @Size(max = 255)
    private String departureLocation;

    @Size(max = 255)
    private String destination;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá người lớn phải lớn hơn 0")
    private BigDecimal priceAdult;

    @DecimalMin(value = "0.0", message = "Giá trẻ em không được âm")
    private BigDecimal priceChild;

    @Size(max = 255)
    private String vehicle;

    @Size(max = 100)
    private String hotelStandard;

    private String includedServices;

    private String excludedServices;

    private String highlights;

    private String notes;

    private Boolean isFeatured;

    private Boolean isActive;
}