package com.bookingtour.notification.mapper;

import com.bookingtour.notification.dto.response.NotificationResponse;
import com.bookingtour.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}