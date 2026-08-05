package com.yuncheng.system.login.auth.service;

import com.yuncheng.framework.security.jwt.JwtTokenService;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.user.service.UserAccountService;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/** 校验短期改密凭据并完成登录前的强制密码修改。 */
@Service
public class RequiredPasswordChangeService {

    private final JwtTokenService jwtTokenService;
    private final UserAccountService userAccountService;

    public RequiredPasswordChangeService(
            JwtTokenService jwtTokenService,
            UserAccountService userAccountService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userAccountService = userAccountService;
    }

    public void change(String token, String newPassword) {
        try {
            Jwt jwt = jwtTokenService.decodePasswordChangeToken(token);
            if (!Boolean.TRUE.equals(jwt.getClaim(JwtTokenService.PASSWORD_CHANGE_REQUIRED_CLAIM))
                    || !ClientType.WEB.name().equals(
                    jwt.getClaimAsString(JwtTokenService.CLIENT_TYPE_CLAIM)
            )) {
                throw invalidToken();
            }
            Long userId = parseUserId(jwt.getSubject());
            Number passwordChangedAt = jwt.getClaim(JwtTokenService.PASSWORD_CHANGED_AT_CLAIM);
            if (passwordChangedAt == null) {
                throw invalidToken();
            }
            userAccountService.completeRequiredPasswordChange(
                    userId,
                    Instant.ofEpochMilli(passwordChangedAt.longValue()),
                    newPassword
            );
        } catch (JwtException | NumberFormatException exception) {
            throw invalidToken();
        }
    }

    private Long parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            throw invalidToken();
        }
        long userId = Long.parseLong(subject);
        if (userId <= 0) {
            throw invalidToken();
        }
        return userId;
    }

    private PlatformException invalidToken() {
        return PlatformException.unauthorized("修改密码凭据无效或已过期，请重新登录");
    }
}
