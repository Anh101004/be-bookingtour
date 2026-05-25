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
public class NotificationResponse {

    private String notificationId;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedId;
    private NotificationRelatedType relatedType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

