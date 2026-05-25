package com.bookingtour.guide.mapper;

import com.bookingtour.guide.dto.request.GuideCreateRequest;
import com.bookingtour.guide.dto.request.GuideScheduleRequest;
import com.bookingtour.guide.dto.request.GuideUpdateRequest;
import com.bookingtour.guide.dto.response.GuideResponse;
import com.bookingtour.guide.dto.response.GuideScheduleResponse;
import com.bookingtour.guide.entity.GuideSchedule;
import com.bookingtour.guide.entity.TourGuide;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface GuideMapper {

    // ==================== TourGuide ====================

    GuideResponse toResponse(TourGuide tourGuide);

    @Mapping(target = "guideId",       ignore = true)
    @Mapping(target = "avatarUrl",     ignore = true)   // xử lý riêng ở service
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalTours",    ignore = true)
    @Mapping(target = "status",        ignore = true)
    @Mapping(target = "isActive",      ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    TourGuide toEntity(GuideCreateRequest request);

    @Mapping(target = "guideId",       ignore = true)
    @Mapping(target = "avatarUrl",     ignore = true)   // xử lý riêng ở service
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalTours",    ignore = true)
    @Mapping(target = "status",        ignore = true)
    @Mapping(target = "isActive",      ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    void updateEntity(GuideUpdateRequest request, @MappingTarget TourGuide tourGuide);

    // ==================== GuideSchedule ====================

    GuideScheduleResponse toScheduleResponse(GuideSchedule guideSchedule);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "guideId",   ignore = true)   // set ở service
    @Mapping(target = "createdAt", ignore = true)
    GuideSchedule toScheduleEntity(GuideScheduleRequest request);
}