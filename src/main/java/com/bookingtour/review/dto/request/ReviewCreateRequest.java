package com.bookingtour.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotBlank(message = "Mã đặt tour không được để trống")
    private String bookingId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5")
    private Integer rating;

    @Size(max = 2000, message = "Nội dung đánh giá không được quá 2000 ký tự")
    private String comment;

    @Min(value = 1, message = "Điểm HDV tối thiểu là 1")
    @Max(value = 5, message = "Điểm HDV tối đa là 5")
    private Integer guideRating;

    private String images; // JSON array URL ảnh
}