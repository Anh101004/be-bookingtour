package com.bookingtour.security;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Lấy userId của người dùng đang đăng nhập.
     * Ném UNAUTHORIZED nếu chưa xác thực.
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetails)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return ((UserDetails) auth.getPrincipal()).getUsername(); // username = userId (xem CustomUserDetailsService)
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails;
    }
}