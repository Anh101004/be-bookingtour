package com.bookingtour.vehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleChangeStatusRequest {

    /** AVAILABLE / IN_USE / MAINTENANCE / RETIRED */
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}