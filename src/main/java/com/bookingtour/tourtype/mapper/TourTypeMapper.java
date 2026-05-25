package com.bookingtour.tourtype.mapper;

import com.bookingtour.tourtype.dto.request.TourTypeCreateRequest;
import com.bookingtour.tourtype.dto.request.TourTypeUpdateRequest;
import com.bookingtour.tourtype.dto.response.TourTypeResponse;
import com.bookingtour.tourtype.entity.TourType;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TourTypeMapper {

    TourTypeResponse toResponse(TourType tourType);

    @Mapping(target = "typeId",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TourType toEntity(TourTypeCreateRequest request);

    @Mapping(target = "typeId",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TourTypeUpdateRequest request, @MappingTarget TourType tourType);
}