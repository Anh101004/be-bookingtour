package com.bookingtour.guide.dto.request;

import com.bookingtour.guide.enums.GuideScheduleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GuideScheduleRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái không được để trống")
    private GuideScheduleStatus status;

    /** ID lịch tour liên kết — tùy chọn */
    private String scheduleId;

    private String note;
}