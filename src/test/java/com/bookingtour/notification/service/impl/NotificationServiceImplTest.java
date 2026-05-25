package com.bookingtour.notification.service.impl;

import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.notification.dto.response.NotificationResponse;
import com.bookingtour.notification.dto.response.NotificationSocketPayload;
import com.bookingtour.notification.dto.response.UnreadCountResponse;
import com.bookingtour.notification.entity.Notification;
import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.mapper.NotificationMapper;
import com.bookingtour.notification.repository.NotificationRepository;
import com.bookingtour.notification.service.NotificationWebSocketService;
import com.bookingtour.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationWebSocketService webSocketService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {

        notification = Notification.builder()
                .notificationId("noti-1")
                .userId("user-1")
                .title("Test")
                .message("Message")
                .type(NotificationType.BOOKING_CONFIRMED)
                .isRead(false)
                .build();
    }

    @Test
    void getMyNotifications_Success() {

        NotificationResponse response =
                new NotificationResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository
                    .findAllByUserIdOrderByCreatedAtDesc("user-1"))
                    .thenReturn(List.of(notification));

            when(notificationMapper.toResponse(any()))
                    .thenReturn(response);

            List<NotificationResponse> result =
                    notificationService.getMyNotifications();

            assertEquals(1, result.size());
        }
    }

    @Test
    void getMyUnread_Success() {

        NotificationResponse response =
                new NotificationResponse();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository
                    .findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc("user-1"))
                    .thenReturn(List.of(notification));

            when(notificationMapper.toResponse(any()))
                    .thenReturn(response);

            List<NotificationResponse> result =
                    notificationService.getMyUnread();

            assertEquals(1, result.size());
        }
    }

    @Test
    void countUnread_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository
                    .countByUserIdAndIsReadFalse("user-1"))
                    .thenReturn(5L);

            UnreadCountResponse result =
                    notificationService.countUnread();

            assertEquals(5L, result.getUnreadCount());
        }
    }

    @Test
    void markAsRead_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository.findById("noti-1"))
                    .thenReturn(Optional.of(notification));

            notificationService.markAsRead("noti-1");

            verify(notificationRepository)
                    .markOneAsRead("noti-1", "user-1");
        }
    }

    @Test
    void markAsRead_NotFound() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository.findById("noti-1"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AppException.class,
                    () -> notificationService.markAsRead("noti-1")
            );
        }
    }

    @Test
    void markAllAsRead_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            notificationService.markAllAsRead();

            verify(notificationRepository)
                    .markAllAsRead("user-1");
        }
    }

    @Test
    void deleteOne_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository
                    .deleteOneByIdAndUserId("noti-1", "user-1"))
                    .thenReturn(1);

            notificationService.deleteOne("noti-1");

            verify(notificationRepository)
                    .deleteOneByIdAndUserId("noti-1", "user-1");
        }
    }

    @Test
    void deleteOne_NotFound() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            when(notificationRepository
                    .deleteOneByIdAndUserId("noti-1", "user-1"))
                    .thenReturn(0);

            assertThrows(
                    AppException.class,
                    () -> notificationService.deleteOne("noti-1")
            );
        }
    }

    @Test
    void deleteAll_Success() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            notificationService.deleteAll();

            verify(notificationRepository)
                    .deleteAllByUserId("user-1");
        }
    }

    @Test
    void send_Success() {

        notificationService.send(
                "user-1",
                NotificationType.BOOKING_CONFIRMED,
                "Title",
                "Message",
                "related-1",
                NotificationRelatedType.BOOKING
        );

        verify(notificationRepository)
                .save(any(Notification.class));

        verify(webSocketService)
                .pushToUser(eq("user-1"), any(Notification.class));
    }

    @Test
    void sendIfNotExists_AlreadyExists() {

        when(notificationRepository
                .existsByRelatedIdAndType(
                        "related-1",
                        NotificationType.BOOKING_CONFIRMED
                )).thenReturn(true);

        notificationService.sendIfNotExists(
                "user-1",
                NotificationType.BOOKING_CONFIRMED,
                "Title",
                "Message",
                "related-1",
                NotificationRelatedType.BOOKING
        );

        verify(notificationRepository, never())
                .save(any());
    }

    @Test
    void sendIfNotExists_Success() {

        when(notificationRepository
                .existsByRelatedIdAndType(
                        "related-1",
                        NotificationType.BOOKING_CONFIRMED
                )).thenReturn(false);

        notificationService.sendIfNotExists(
                "user-1",
                NotificationType.BOOKING_CONFIRMED,
                "Title",
                "Message",
                "related-1",
                NotificationRelatedType.BOOKING
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void getByUserId_Success() {

        NotificationResponse response =
                new NotificationResponse();

        when(notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(notification));

        when(notificationMapper.toResponse(any()))
                .thenReturn(response);

        List<NotificationResponse> result =
                notificationService.getByUserId("user-1");

        assertEquals(1, result.size());
    }

    @Test
    void broadcast_Success() {

        User user1 = new User();
        user1.setUserId("user-1");
        user1.setIsActive(true);

        User user2 = new User();
        user2.setUserId("user-2");
        user2.setIsActive(true);

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        notificationService.broadcast(
                NotificationType.SYSTEM,
                "System",
                "Maintenance"
        );

        verify(notificationRepository, times(2))
                .save(any(Notification.class));

        verify(webSocketService)
                .pushBroadcast(any(NotificationSocketPayload.class));
    }

    @Test
    void broadcast_IgnoreInactiveUsers() {

        User active = new User();
        active.setUserId("user-1");
        active.setIsActive(true);

        User inactive = new User();
        inactive.setUserId("user-2");
        inactive.setIsActive(false);

        when(userRepository.findAll())
                .thenReturn(List.of(active, inactive));

        notificationService.broadcast(
                NotificationType.SYSTEM,
                "System",
                "Maintenance"
        );

        verify(notificationRepository, times(1))
                .save(any(Notification.class));
    }
}