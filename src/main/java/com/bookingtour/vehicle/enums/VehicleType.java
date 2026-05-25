package com.bookingtour.vehicle.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

    BUS     ("Xe khách"),
    VAN     ("Xe van / minibus"),
    BOAT    ("Tàu thuyền"),
    AIRPLANE("Máy bay"),
    TRAIN   ("Tàu hỏa");

    private final String label;
}