package com.bookingtour.schedule.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleVehicleResponse {

    private Integer       id;
    private String        vehicleId;
    private String        vehicleName;
    private String        vehicleType;
    private String        vehicleTypeLabel;
    private String        licensePlate;
    private Integer       capacity;
    private String        features;
    private String        vehicleImageUrl;
    private String        legDescription;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String        estimatedDuration;
    private BigDecimal    pricePerPerson;
    private String        notes;
    private LocalDateTime createdAt;
}