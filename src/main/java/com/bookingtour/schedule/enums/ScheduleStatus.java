package com.bookingtour.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleStatus {

    AVAILABLE ("Còn chỗ"),
    FULL      ("Hết chỗ"),
    DEPARTED  ("Đã khởi hành"),
    COMPLETED ("Đã hoàn thành"),
    CANCELLED ("Đã hủy");

    private final String label;
}