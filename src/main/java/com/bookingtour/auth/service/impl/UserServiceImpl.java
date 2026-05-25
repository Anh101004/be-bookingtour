package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.dto.request.UpdateProfileRequest;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.mapper.UserMapper;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.auth.service.IUserService;
import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository          userRepository;
    private final UserMapper              userMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    // ==================== Lấy thông tin cá nhân ====================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        return userMapper.toResponse(getCurrentUser());
    }

    // ==================== Cập nhật thông tin cá nhân ====================

    @Override
    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        userMapper.updateEntity(request, user);
        userRepository.save(user);
        log.info("Cập nhật profile user: {}", user.getUsername());
        return userMapper.toResponse(user);
    }

    // ==================== Upload avatar ====================

    @Override
    @Transactional
    public UserResponse uploadMyAvatar(MultipartFile file) {
        User user = getCurrentUser();

        // Xóa ảnh cũ nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            cloudinaryStorageService.deleteFile(user.getAvatarUrl());
        }

        String newAvatarUrl = cloudinaryStorageService.uploadImage(
                file, CloudinaryStorageService.FOLDER_USER_AVATAR);

        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);

        log.info("Upload avatar thành công cho user: {}", user.getUsername());
        return userMapper.toResponse(user);
    }

    // ==================== Admin ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse toggleUserActive(String userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(isActive);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    // ==================== Private ====================

    private User getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}