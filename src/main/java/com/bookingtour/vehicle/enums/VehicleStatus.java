package com.bookingtour.vehicle.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleStatus {

    AVAILABLE  ("Sẵn sàng"),
    IN_USE     ("Đang sử dụng"),
    MAINTENANCE("Đang bảo trì"),
    RETIRED    ("Ngừng hoạt động");

    private final String label;
}