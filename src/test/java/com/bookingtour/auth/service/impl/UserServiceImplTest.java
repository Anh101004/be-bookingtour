package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.dto.request.UpdateProfileRequest;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.mapper.UserMapper;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
import com.bookingtour.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .userId("u1")
                .username("admin")
                .fullName("Admin")
                .avatarUrl("old-avatar")
                .build();
    }

    @Test
    void getMyProfile_success() {

        UserResponse response = UserResponse.builder()
                .userId("u1")
                .username("admin")
                .build();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("u1");

            when(userRepository.findById("u1"))
                    .thenReturn(Optional.of(user));

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            UserResponse result = userService.getMyProfile();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("admin");
        }
    }

    @Test
    void getMyProfile_userNotFound() {

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("u1");

            when(userRepository.findById("u1"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.getMyProfile())
                    .isInstanceOf(AppException.class);
        }
    }

    @Test
    void updateMyProfile_success() {

        UpdateProfileRequest request =
                new UpdateProfileRequest();

        request.setFullName("New Name");

        UserResponse response = UserResponse.builder()
                .userId("u1")
                .fullName("New Name")
                .build();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("u1");

            when(userRepository.findById("u1"))
                    .thenReturn(Optional.of(user));

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            UserResponse result =
                    userService.updateMyProfile(request);

            assertThat(result.getFullName())
                    .isEqualTo("New Name");

            verify(userMapper)
                    .updateEntity(request, user);

            verify(userRepository)
                    .save(user);
        }
    }

    @Test
    void uploadMyAvatar_success() {

        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "abc".getBytes()
        );

        UserResponse response = UserResponse.builder()
                .userId("u1")
                .avatarUrl("new-avatar")
                .build();

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("u1");

            when(userRepository.findById("u1"))
                    .thenReturn(Optional.of(user));

            when(cloudinaryStorageService.uploadImage(
                    any(),
                    eq(CloudinaryStorageService.FOLDER_USER_AVATAR)
            )).thenReturn("new-avatar");

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            UserResponse result =
                    userService.uploadMyAvatar(file);

            assertThat(result.getAvatarUrl())
                    .isEqualTo("new-avatar");

            verify(cloudinaryStorageService)
                    .deleteFile("old-avatar");

            verify(cloudinaryStorageService)
                    .uploadImage(any(), any());
        }
    }

    @Test
    void uploadMyAvatar_withoutOldAvatar() {

        user.setAvatarUrl(null);

        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "abc".getBytes()
        );

        try (MockedStatic<SecurityUtils> mocked =
                     mockStatic(SecurityUtils.class)) {

            mocked.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("u1");

            when(userRepository.findById("u1"))
                    .thenReturn(Optional.of(user));

            when(cloudinaryStorageService.uploadImage(
                    any(),
                    any()
            )).thenReturn("new-avatar");

            when(userMapper.toResponse(any()))
                    .thenReturn(UserResponse.builder().build());

            userService.uploadMyAvatar(file);

            verify(cloudinaryStorageService, never())
                    .deleteFile(any());
        }
    }

    @Test
    void getAllUsers_success() {

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        when(userMapper.toResponse(any()))
                .thenReturn(UserResponse.builder().build());

        List<UserResponse> result =
                userService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserById_success() {

        when(userRepository.findById("u1"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(UserResponse.builder().build());

        UserResponse result =
                userService.getUserById("u1");

        assertThat(result).isNotNull();
    }

    @Test
    void getUserById_notFound() {

        when(userRepository.findById("u1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getUserById("u1"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void toggleUserActive_success() {

        when(userRepository.findById("u1"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(any()))
                .thenReturn(UserResponse.builder().build());

        userService.toggleUserActive("u1", false);

        assertThat(user.getIsActive()).isFalse();

        verify(userRepository).save(user);
    }
}