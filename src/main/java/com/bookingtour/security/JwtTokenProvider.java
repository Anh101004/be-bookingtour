package com.bookingtour.security;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;          // access token: 15 phút

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;      // refresh token: 7 ngày

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ==================== Tạo Access Token ====================
    public String generateAccessToken(String userId, String username, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==================== Tạo Refresh Token ====================
    public String generateRefreshToken(String userId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==================== Lấy userId từ token ====================
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // ==================== Validate token ====================
    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }

    public long getExpirationSeconds() {
        return jwtExpirationMs / 1000;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    // ==================== Private ====================
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}