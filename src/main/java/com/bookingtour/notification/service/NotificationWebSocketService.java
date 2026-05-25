package com.bookingtour.notification.service;

import com.bookingtour.notification.dto.response.NotificationSocketPayload;
import com.bookingtour.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push tới 1 user cụ thể (đang online).
     * Client subscribe: /user/{userId}/queue/notifications
     */
    public void pushToUser(String userId, Notification notification) {
        try {
            NotificationSocketPayload payload = toPayload(notification);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    payload
            );
            log.info("WS push tới userId={} | type={}", userId, notification.getType());
        } catch (Exception e) {
            // User offline → không sao, họ lấy qua REST khi online lại
            log.debug("WS push thất bại userId={} (có thể đang offline): {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast tới tất cả đang online.
     * Client subscribe: /topic/notifications
     */
    public void pushBroadcast(NotificationSocketPayload payload) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", payload);
            log.info("WS broadcast | type={}", payload.getType());
        } catch (Exception e) {
            log.warn("WS broadcast thất bại: {}", e.getMessage());
        }
    }

    private NotificationSocketPayload toPayload(Notification notification) {
        return NotificationSocketPayload.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}