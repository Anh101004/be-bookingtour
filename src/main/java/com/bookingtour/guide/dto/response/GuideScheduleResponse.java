package com.bookingtour.guide.dto.response;

import com.bookingtour.guide.enums.GuideScheduleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuideScheduleResponse {

    private Integer id;
    private String guideId;
    private String scheduleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private GuideScheduleStatus status;
    private String note;
    private LocalDateTime createdAt;
}