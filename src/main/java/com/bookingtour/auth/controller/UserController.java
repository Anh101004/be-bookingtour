package com.bookingtour.auth.controller;

import com.bookingtour.auth.dto.request.UpdateProfileRequest;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.service.IUserService;
import com.bookingtour.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /** GET /api/users/me — Lấy thông tin cá nhân */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile()));
    }

    /** PUT /api/users/me — Cập nhật thông tin cá nhân */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", response));
    }

    /** POST /api/users/me/avatar — Upload ảnh đại diện */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestPart("file") MultipartFile file) {
        UserResponse response = userService.uploadMyAvatar(file);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", response));
    }

    /** GET /api/users — ADMIN: danh sách tất cả */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    /** GET /api/users/{userId} — ADMIN: lấy theo ID */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    /** PATCH /api/users/{userId}/active — ADMIN: bật/tắt tài khoản */
    @PatchMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @PathVariable String userId,
            @RequestParam boolean isActive) {
        UserResponse updated = userService.toggleUserActive(userId, isActive);
        String msg = isActive ? "Đã kích hoạt tài khoản" : "Đã vô hiệu hóa tài khoản";
        return ResponseEntity.ok(ApiResponse.success(msg, updated));
    }
}