package com.bookingtour.auth.service;

import com.bookingtour.auth.dto.request.UpdateProfileRequest;
import com.bookingtour.auth.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {

    UserResponse getMyProfile();

    UserResponse updateMyProfile(UpdateProfileRequest request);

    UserResponse uploadMyAvatar(MultipartFile file);

    // Admin
    List<UserResponse> getAllUsers();

    UserResponse getUserById(String userId);

    UserResponse toggleUserActive(String userId, boolean isActive);
}