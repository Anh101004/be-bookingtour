package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.dto.request.*;
import com.bookingtour.auth.dto.response.LoginResponse;
import com.bookingtour.auth.dto.response.TokenResponse;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.enums.UserRole;
import com.bookingtour.auth.mapper.UserMapper;
import com.bookingtour.auth.repository.ActivityLogRepository;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.auth.service.IEmailService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IEmailService emailService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                authService,
                "otpExpiryMinutes",
                10
        );

        user = User.builder()
                .userId("u1")
                .username("admin")
                .email("admin@gmail.com")
                .passwordHash("encodedPassword")
                .fullName("Admin")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .emailVerified(true)
                .build();
    }

    // ================= REGISTER =================

    @Test
    void register_success() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setEmail("admin@gmail.com");
        request.setPassword("123456");
        request.setFullName("Admin");

        UserResponse response = UserResponse.builder()
                .userId("u1")
                .username("admin")
                .build();

        when(userRepository.existsByUsername("admin"))
                .thenReturn(false);

        when(userRepository.existsByEmail("admin@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded");

        when(userMapper.toResponse(any(User.class)))
                .thenReturn(response);

        UserResponse result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_usernameExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");

        when(userRepository.existsByUsername("admin"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void register_emailExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setEmail("admin@gmail.com");

        when(userRepository.existsByUsername("admin"))
                .thenReturn(false);

        when(userRepository.existsByEmail("admin@gmail.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(request))
                .isInstanceOf(AppException.class);
    }

    // ================= LOGIN =================

    @Test
    void login_success() {

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        UserResponse userResponse = UserResponse.builder()
                .userId("u1")
                .username("admin")
                .build();

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "123456",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtTokenProvider.generateAccessToken(
                any(),
                any(),
                any()
        )).thenReturn("access-token");

        when(jwtTokenProvider.generateRefreshToken(any()))
                .thenReturn("refresh-token");

        when(jwtTokenProvider.getRefreshExpirationMs())
                .thenReturn(86400000L);

        when(jwtTokenProvider.getExpirationSeconds())
                .thenReturn(3600L);

        when(userMapper.toResponse(any(User.class)))
                .thenReturn(userResponse);

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        LoginResponse result =
                authService.login(request, httpServletRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_userNotFound() {

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(request, httpServletRequest))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_wrongPassword() {

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong",
                "encodedPassword"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(request, httpServletRequest))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_accountDisabled() {

        user.setIsActive(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "123456",
                "encodedPassword"
        )).thenReturn(true);

        assertThatThrownBy(() ->
                authService.login(request, httpServletRequest))
                .isInstanceOf(AppException.class);
    }

    // ================= REFRESH TOKEN =================

    @Test
    void refreshToken_success() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("refresh");

        user.setRefreshToken("refresh");
        user.setRefreshTokenExpiry(
                LocalDateTime.now().plusDays(1)
        );

        when(jwtTokenProvider.getUserIdFromToken("refresh"))
                .thenReturn("u1");

        when(userRepository.findById("u1"))
                .thenReturn(Optional.of(user));

        when(jwtTokenProvider.generateAccessToken(
                any(),
                any(),
                any()
        )).thenReturn("new-access");

        when(jwtTokenProvider.generateRefreshToken(any()))
                .thenReturn("new-refresh");

        when(jwtTokenProvider.getRefreshExpirationMs())
                .thenReturn(86400000L);

        when(jwtTokenProvider.getExpirationSeconds())
                .thenReturn(3600L);

        TokenResponse result =
                authService.refreshToken(request);

        assertThat(result).isNotNull();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void refreshToken_invalid() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("invalid");

        doThrow(new AppException(
                ErrorCode.REFRESH_TOKEN_INVALID
        )).when(jwtTokenProvider)
                .validateToken("invalid");

        assertThatThrownBy(() ->
                authService.refreshToken(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void refreshToken_userNotFound() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("refresh");

        when(jwtTokenProvider.getUserIdFromToken("refresh"))
                .thenReturn("u1");

        when(userRepository.findById("u1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.refreshToken(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void refreshToken_expired() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("refresh");

        user.setRefreshToken("refresh");

        user.setRefreshTokenExpiry(
                LocalDateTime.now().minusDays(1)
        );

        when(jwtTokenProvider.getUserIdFromToken("refresh"))
                .thenReturn("u1");

        when(userRepository.findById("u1"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.refreshToken(request))
                .isInstanceOf(AppException.class);
    }

    // ================= FORGOT PASSWORD =================

    @Test
    void forgotPassword_success() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        doNothing().when(emailService)
                .sendOtpEmail(any(), any(), any());

        authService.forgotPassword(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void forgotPassword_userNotFound() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail("abc@gmail.com");

        when(userRepository.findByEmail("abc@gmail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.forgotPassword(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void forgotPassword_emailFail() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException())
                .when(emailService)
                .sendOtpEmail(any(), any(), any());

        assertThatThrownBy(() ->
                authService.forgotPassword(request))
                .isInstanceOf(AppException.class);
    }

    // ================= RESET PASSWORD =================

    @Test
    void resetPassword_success() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("admin@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newpass");
        request.setConfirmPassword("newpass");

        user.setResetOtp("123456");

        user.setResetOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newpass"))
                .thenReturn("encoded");

        authService.resetPassword(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_confirmMismatch() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setNewPassword("123");
        request.setConfirmPassword("456");

        assertThatThrownBy(() ->
                authService.resetPassword(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void resetPassword_userNotFound() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("admin@gmail.com");
        request.setOtp("123");
        request.setNewPassword("123");
        request.setConfirmPassword("123");

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.resetPassword(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void resetPassword_invalidOtp() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("admin@gmail.com");
        request.setOtp("wrong");
        request.setNewPassword("123");
        request.setConfirmPassword("123");

        user.setResetOtp("123456");

        user.setResetOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.resetPassword(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void resetPassword_expiredOtp() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("admin@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("123");
        request.setConfirmPassword("123");

        user.setResetOtp("123456");

        user.setResetOtpExpiry(
                LocalDateTime.now().minusMinutes(1)
        );

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.resetPassword(request))
                .isInstanceOf(AppException.class);
    }
}