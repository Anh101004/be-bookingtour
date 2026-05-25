package com.bookingtour.notification.controller;

import com.bookingtour.notification.dto.request.BroadcastRequest;
import com.bookingtour.notification.dto.response.NotificationResponse;
import com.bookingtour.notification.dto.response.UnreadCountResponse;
import com.bookingtour.notification.service.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │  USER (isAuthenticated)                                                   │
 * │  GET    /api/notifications                ds thông báo của tôi           │
 * │  GET    /api/notifications/unread         ds chưa đọc                    │
 * │  GET    /api/notifications/unread/count   số lượng chưa đọc              │
 * │  PATCH  /api/notifications/{id}/read      đánh dấu 1 đã đọc             │
 * │  PATCH  /api/notifications/read-all       đánh dấu tất cả đã đọc        │
 * │  DELETE /api/notifications/{id}           xóa 1 thông báo               │
 * │  DELETE /api/notifications                xóa tất cả thông báo          │
 * ├──────────────────────────────────────────────────────────────────────────┤
 * │  ADMIN                                                                    │
 * │  GET    /api/notifications/admin/user/{userId}   thông báo của 1 user    │
 * │  POST   /api/notifications/admin/broadcast       gửi broadcast           │
 * └──────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    // ── USER ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnread() {
        return ResponseEntity.ok(notificationService.getMyUnread());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> countUnread() {
        return ResponseEntity.ok(notificationService.countUnread());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteOne(@PathVariable String notificationId) {
        notificationService.deleteOne(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        notificationService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    // ── ADMIN ────────────────────────────────────────────────────────────────

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getByUserId(userId));
    }

    @PostMapping("/admin/broadcast")
    public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastRequest request) {
        notificationService.broadcast(request.getType(), request.getTitle(), request.getMessage());
        return ResponseEntity.noContent().build();
    }
}