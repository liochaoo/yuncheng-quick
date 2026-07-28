package com.yuncheng.framework.security.context;

import com.yuncheng.common.context.CurrentSessionContext;
import com.yuncheng.framework.security.jwt.JwtTokenService;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** 从 Spring Security JWT 认证结果读取当前登录会话标识。 */
@Component
public class SecurityCurrentSessionContext implements CurrentSessionContext {

    @Override
    public Optional<String> findSessionId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof CurrentUserJwtAuthenticationToken currentAuthentication)
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                currentAuthentication.getToken().getClaimAsString(JwtTokenService.SESSION_ID_CLAIM)
        ).filter(sessionId -> !sessionId.isBlank());
    }
}
