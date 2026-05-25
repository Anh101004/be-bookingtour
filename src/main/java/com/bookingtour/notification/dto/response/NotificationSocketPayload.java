package com.bookingtour.notification.dto.response;

import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationSocketPayload {

    private String notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedId;
    private NotificationRelatedType relatedType;
    private LocalDateTime createdAt;
}