package com.bookingtour.notification.service;

import com.bookingtour.notification.dto.response.NotificationResponse;
import com.bookingtour.notification.dto.response.UnreadCountResponse;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;

import java.util.List;

public interface INotificationService {

    // ── User ─────────────────────────────────────────────────────────────────
    List<NotificationResponse> getMyNotifications();
    List<NotificationResponse> getMyUnread();
    UnreadCountResponse         countUnread();
    void markAsRead(String notificationId);
    void markAllAsRead();
    void deleteOne(String notificationId);
    void deleteAll();

    // ── Internal ─────────────────────────────────────────────────────────────
    void send(String userId,
              NotificationType type,
              String title,
              String message,
              String relatedId,
              NotificationRelatedType relatedType);

    void sendIfNotExists(String userId,
                         NotificationType type,
                         String title,
                         String message,
                         String relatedId,
                         NotificationRelatedType relatedType);

    // ── Admin ────────────────────────────────────────────────────────────────
    List<NotificationResponse> getByUserId(String userId);
    void broadcast(NotificationType type, String title, String message);
}