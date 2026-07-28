package com.yuncheng.system.login.auth.support;

import com.yuncheng.system.login.auth.config.RefreshCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** 写入和清除 WEB Refresh Token Cookie。 */
@Component
public class RefreshCookieManager {

    private final RefreshCookieProperties properties;

    public RefreshCookieManager(RefreshCookieProperties properties) {
        this.properties = properties;
    }

    public void write(HttpServletResponse response, String refreshToken, Instant expiresAt) {
        Duration maxAge = Duration.between(Instant.now(), expiresAt);
        ResponseCookie cookie = ResponseCookie.from(properties.getName(), refreshToken)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath())
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(properties.getName(), "")
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath())
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (properties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
