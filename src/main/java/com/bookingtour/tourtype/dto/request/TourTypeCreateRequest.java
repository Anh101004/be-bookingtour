package com.bookingtour.tourtype.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourTypeCreateRequest {

    @NotBlank(message = "Tên loại tour không được để trống")
    @Size(max = 255, message = "Tên loại tour không được quá 255 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 255, message = "Slug không được quá 255 ký tự")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug chỉ chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    private String description;

    @Size(max = 500, message = "URL icon không được quá 500 ký tự")
    private String iconUrl;

    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder = 0;

    private Boolean isActive = true;
}