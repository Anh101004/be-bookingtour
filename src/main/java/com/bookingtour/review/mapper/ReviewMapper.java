package com.bookingtour.review.mapper;

import com.bookingtour.review.dto.response.ReviewResponse;
import com.bookingtour.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReviewMapper {

    // userFullName, userAvatarUrl, tourTitle set thủ công trong service
    @Mapping(target = "userFullName",  ignore = true)
    @Mapping(target = "userAvatarUrl", ignore = true)
    @Mapping(target = "tourTitle",     ignore = true)
    ReviewResponse toResponse(Review review);
}