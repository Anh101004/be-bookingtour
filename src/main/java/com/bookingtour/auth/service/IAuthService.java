package com.bookingtour.auth.service;

import com.bookingtour.auth.dto.request.*;
import com.bookingtour.auth.dto.response.LoginResponse;
import com.bookingtour.auth.dto.response.TokenResponse;
import com.bookingtour.auth.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout();

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}