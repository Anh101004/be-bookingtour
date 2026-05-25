package com.bookingtour.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationCreateRequest {

    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}