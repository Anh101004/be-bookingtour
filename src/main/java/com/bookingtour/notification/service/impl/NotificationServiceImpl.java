package com.bookingtour.notification.service.impl;

import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.notification.dto.response.NotificationResponse;
import com.bookingtour.notification.dto.response.NotificationSocketPayload;
import com.bookingtour.notification.dto.response.UnreadCountResponse;
import com.bookingtour.notification.entity.Notification;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.mapper.NotificationMapper;
import com.bookingtour.notification.repository.NotificationRepository;
import com.bookingtour.notification.service.INotificationService;
import com.bookingtour.notification.service.NotificationWebSocketService;
import com.bookingtour.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository       notificationRepository;
    private final NotificationMapper           notificationMapper;
    private final UserRepository               userRepository;
    private final NotificationWebSocketService webSocketService;

    // ════════════════════════════════════════════════════════════════════════
    // USER
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnread() {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationRepository
                .findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread() {
        String userId = SecurityUtils.getCurrentUserId();
        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void markAsRead(String notificationId) {
        String userId = SecurityUtils.getCurrentUserId();
        notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notificationRepository.markOneAsRead(notificationId, userId);
        log.info("Đánh dấu đã đọc notificationId={} userId={}", notificationId, userId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsRead(userId);
        log.info("Đánh dấu tất cả đã đọc userId={}", userId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteOne(String notificationId) {
        String userId = SecurityUtils.getCurrentUserId();
        int deleted = notificationRepository.deleteOneByIdAndUserId(notificationId, userId);
        if (deleted == 0) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        log.info("User {} xóa thông báo {}", userId, notificationId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteAll() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationRepository.deleteAllByUserId(userId);
        log.info("User {} xóa tất cả thông báo", userId);
    }

    // ════════════════════════════════════════════════════════════════════════
    // INTERNAL
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void send(String userId,
                     NotificationType type,
                     String title,
                     String message,
                     String relatedId,
                     NotificationRelatedType relatedType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Tạo thông báo [{}] cho userId={}", type, userId);

        webSocketService.pushToUser(userId, notification);
    }

    @Override
    @Transactional
    public void sendIfNotExists(String userId,
                                NotificationType type,
                                String title,
                                String message,
                                String relatedId,
                                NotificationRelatedType relatedType) {
        if (relatedId != null
                && notificationRepository.existsByRelatedIdAndType(relatedId, type)) {
            log.debug("Bỏ qua thông báo trùng: relatedId={} type={}", relatedId, type);
            return;
        }
        send(userId, type, title, message, relatedId, relatedType);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUserId(String userId) {
        return notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void broadcast(NotificationType type, String title, String message) {
        List<String> userIds = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .map(u -> u.getUserId())
                .toList();

        for (String userId : userIds) {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        NotificationSocketPayload payload = NotificationSocketPayload.builder()
                .type(type).title(title).message(message).build();
        webSocketService.pushBroadcast(payload);

        log.info("Broadcast [{}] tới {} users", type, userIds.size());
    }
}