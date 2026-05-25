package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.dto.request.*;
import com.bookingtour.auth.dto.response.LoginResponse;
import com.bookingtour.auth.dto.response.TokenResponse;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.entity.ActivityLog;
import com.bookingtour.auth.entity.User;
import com.bookingtour.auth.enums.ActivityType;
import com.bookingtour.auth.enums.UserRole;
import com.bookingtour.auth.mapper.UserMapper;
import com.bookingtour.auth.repository.ActivityLogRepository;
import com.bookingtour.auth.repository.UserRepository;
import com.bookingtour.auth.service.IAuthService;
import com.bookingtour.auth.service.IEmailService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.security.JwtTokenProvider;
import com.bookingtour.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository        userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserMapper            userMapper;
    private final IEmailService         emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    // ==================== ĐĂNG KÝ ====================
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        log.info("Đăng ký thành công: {}", user.getUsername());

        saveActivityLog(user.getUserId(), ActivityType.REGISTER, null, null, null);
        return userMapper.toResponse(user);
    }

    // ==================== ĐĂNG NHẬP ====================
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getUserId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshExpirationMs() / 1000));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        saveActivityLog(user.getUserId(), ActivityType.LOGIN, null, null,
                httpRequest.getRemoteAddr());

        return LoginResponse.builder()
                .user(userMapper.toResponse(user))
                .token(TokenResponse.of(accessToken, refreshToken,
                        jwtTokenProvider.getExpirationSeconds()))
                .build();
    }

    // ==================== REFRESH TOKEN ====================
    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String incomingRefresh = request.getRefreshToken();

        try {
            jwtTokenProvider.validateToken(incomingRefresh);
        } catch (AppException e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(incomingRefresh);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!incomingRefresh.equals(user.getRefreshToken())
                || user.getRefreshTokenExpiry() == null
                || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String newAccessToken  = jwtTokenProvider.generateAccessToken(
                user.getUserId(), user.getUsername(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshExpirationMs() / 1000));
        userRepository.save(user);

        return TokenResponse.of(newAccessToken, newRefreshToken,
                jwtTokenProvider.getExpirationSeconds());
    }

    // ==================== ĐĂNG XUẤT ====================
    @Override
    @Transactional
    public void logout() {
        String userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        saveActivityLog(userId, ActivityType.LOGOUT, null, null, null);
    }

    // ==================== QUÊN MẬT KHẨU ====================
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
        } catch (Exception e) {
            log.error("Gửi OTP thất bại cho email: {}", user.getEmail(), e);
            throw new AppException(ErrorCode.OTP_SEND_FAILED);
        }

        log.info("Đã gửi OTP reset mật khẩu tới: {}", user.getEmail());
    }

    // ==================== ĐẶT LẠI MẬT KHẨU ====================
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        if (user.getResetOtpExpiry() == null
                || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        saveActivityLog(user.getUserId(), ActivityType.RESET_PASSWORD, null, null, null);
    }

    // ==================== ĐỔI MẬT KHẨU ====================
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Mật khẩu xác nhận không khớp");
        }

        String userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        saveActivityLog(userId, ActivityType.CHANGE_PASSWORD, null, null, null);
    }

    // ==================== Private helpers ====================
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void saveActivityLog(String userId, ActivityType type,
                                 String targetId, String targetType, String ip) {
        try {
            ActivityLog actLog = ActivityLog.builder()
                    .userId(userId)
                    .activityType(type)
                    .targetId(targetId)
                    .targetType(targetType)
                    .ipAddress(ip)
                    .build();
            activityLogRepository.save(actLog);
        } catch (Exception e) {
            log.warn("Không lưu được activity log: {}", e.getMessage());
        }
    }
}