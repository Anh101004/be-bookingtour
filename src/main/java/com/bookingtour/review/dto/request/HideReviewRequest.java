package com.bookingtour.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HideReviewRequest {

    @NotBlank(message = "Lý do ẩn không được để trống")
    @Size(max = 500, message = "Lý do không được quá 500 ký tự")
    private String hiddenReason;
}