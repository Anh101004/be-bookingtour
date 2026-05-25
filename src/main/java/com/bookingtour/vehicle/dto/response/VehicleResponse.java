package com.bookingtour.vehicle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VehicleResponse {

    private String vehicleId;
    private String name;

    /** BUS / VAN / BOAT / AIRPLANE / TRAIN */
    private String type;
    /** Nhãn tiếng Việt: "Xe khách", "Tàu thuyền"... */
    private String typeLabel;

    private String  licensePlate;
    private Integer capacity;
    private String  brand;
    private String  model;
    private Integer manufactureYear;
    private String  color;
    private String  features;
    private String  imageUrl;

    /**
     * Giá thuê phương tiện mỗi ngày (VNĐ).
     * Schedule module dùng để tính chi phí vận chuyển theo từng chặng.
     */
    private BigDecimal pricePerDay;

    /** AVAILABLE / IN_USE / MAINTENANCE / RETIRED */
    private String status;
    /** Nhãn tiếng Việt: "Sẵn sàng", "Đang sử dụng"... */
    private String statusLabel;

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}