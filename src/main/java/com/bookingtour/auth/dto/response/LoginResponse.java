package com.bookingtour.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private UserResponse user;
    private TokenResponse token;
}